package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.emptyStateEvent
import com.utsman.chatingan.common.event.errorStateEvent
import com.utsman.chatingan.common.event.getExceptionOfNull
import com.utsman.chatingan.common.event.invoke
import com.utsman.chatingan.common.event.map
import com.utsman.chatingan.network.asFlowEvent
import com.utsman.chatingan.sdk.data.FirebaseMessageRequest
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.store.ContactStore
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import com.utsman.chatingan.sdk.services.FirebaseServices
import com.utsman.chatingan.sdk.storage.ChatInfoStorage
import com.utsman.chatingan.sdk.storage.ChatStorage
import com.utsman.chatingan.sdk.storage.MessageChatStorage
import com.utsman.chatingan.sdk.storage.ContactStorage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.util.*

internal class ChatinganImpl(
    private val contactStorage: ContactStorage,
    private val chatinganConfig: ChatinganConfig,
) : Chatingan {

    private val firebaseServices: FirebaseServices by lazy {
        FirebaseServices.instance(chatinganConfig)
    }

    override val config: ChatinganConfig
        get() = chatinganConfig

    private val chatStorage: ChatStorage by lazy {
        ChatStorage(contactStorage, config)
    }

    private val chatInfoStorage: ChatInfoStorage by lazy {
        ChatInfoStorage()
    }

    private val chatInfoIdFinder: ChatInfoStorage.IdFinder by lazy {
        ChatInfoStorage.IdFinder(chatinganConfig)
    }

    override suspend fun updateFcm(fcmToken: String): FlowEvent<String> {
        val currentContact = config.contact
        return if (currentContact != Contact()) {
            addMeContact(currentContact)
                .map { state ->
                    state.map {
                        fcmToken
                    }
                }.stateIn(IOScope())
        } else {
            defaultStateEvent()
        }
    }

    override suspend fun addMeContact(contact: Contact): FlowEvent<Contact> {
        val fcmToken = config.fcmToken
        val contactStore = ContactStore.fromContact(contact, fcmToken)
        return contactStorage.addItem(contactStore, contact.id)
    }

    override suspend fun contacts(): FlowEvent<List<Contact>> {
        return contactStorage.listenItem()
    }

    @OptIn(FlowPreview::class)
    override suspend fun sendMessage(
        contact: Contact,
        messageChat: MessageChat,
        chatInfo: ChatInfo?
    ): FlowEvent<MessageChat> {
        val messageChatStore = MessageChatStore.build(
            senderId = messageChat.senderId,
            receiverId = messageChat.receiverId,
            message = messageChat.messageBody,
            date = messageChat.lastUpdate
        )

        val messageRequest = FirebaseMessageRequest.createFromMessage(
            messageChat = messageChat,
            title = contact.name,
            token = contact.name
        )

        val pathId = chatInfoIdFinder.getForContact(contact) ?: UUID.randomUUID().toString()
        val messageChatStorage = MessageChatStorage(pathId)

        val currentChatInfo = chatInfoStorage.findItemById(pathId)

        val currentChatInfoId = currentChatInfo?.id
        val currentChatInfoReadByIds = currentChatInfo?.readByIds

        val newChatInfo = ChatInfo(
            id = currentChatInfoId ?: pathId,
            lastMessage = messageChat.messageBody,
            memberIds = listOf(messageChat.senderId, messageChat.receiverId),
            readByIds = currentChatInfoReadByIds ?: listOf(messageChat.senderId),
            lastUpdate = messageChat.lastUpdate
        )

        return firebaseServices.sendMessage(messageRequest)
            .asFlowEvent()
            .flatMapMerge {
                chatInfoStorage.addItem(newChatInfo.toStore(), newChatInfo.id, isMerge = true)
                    .flatMapMerge {
                        messageChatStorage.addItem(messageChatStore, messageChatStore.id, isMerge = false)
                    }
            }
            .stateIn(IOScope())
    }

    override suspend fun createMessageChat(
        senderId: String,
        receiverId: String,
        message: String
    ): MessageChat {
        return MessageChatStore.build(
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            date = Date.from(Instant.now())
        ).toMessageChat()
    }

    override suspend fun getMessages(contact: Contact): FlowEvent<List<MessageChat>> {
        val pathId = chatInfoIdFinder.getForContact(contact) ?: return emptyStateEvent()
        val messageChatStorage = MessageChatStorage(pathId)
        return messageChatStorage.listenItem()
    }

    @OptIn(FlowPreview::class)
    override suspend fun getChat(contact: Contact): FlowEvent<Chat> {
        var chatInfo = ChatInfo()
        return chatInfoIdFinder.listenForContact(contact)
            .map {
                it.map { pathId ->
                    chatInfo = chatInfoStorage.findItemById(pathId) ?: ChatInfo()
                    MessageChatStorage(pathId)
                }
            }.flatMapMerge {
                val storage = it.invoke()
                storage?.listenItem() ?: emptyStateEvent()
            }
            .debounce(500)
            .map {
                it.map { messages ->
                    Chat(
                        id = chatInfo.id,
                        contacts = listOf(config.contact, contact),
                        messages = messages,
                        chatInfo = chatInfo
                    )
                }
            }
            .map {
                val chatId = it.invoke()?.id.orEmpty()
                if (chatId.isEmpty()) {
                    StateEvent.Empty()
                } else {
                    it
                }
            }
            .stateIn(IOScope())
    }

    override suspend fun getChatInfos(contact: Contact): FlowEvent<List<ChatInfo>> {
        return chatInfoStorage.listenItem()
    }

    override suspend fun getChatInfo(contact: Contact): FlowEvent<ChatInfo> {
        val id = chatInfoStorage.path()
        return chatInfoStorage.findItemByIdFlow(id).stateIn(IOScope())
    }

    override suspend fun getChats(): FlowEvent<List<Chat>> {
        return chatStorage.getChatList()
    }

    override suspend fun markChatRead(contacts: List<Contact>): FlowEvent<ChatInfo> {
       return defaultStateEvent()
    }


    override suspend fun tokenForId(id: String): FlowEvent<String> {
        return contactStorage.findItemStoreByIdFlow(id).map {
            it.map { data -> data.token }
        }.stateIn(IOScope())
    }
}