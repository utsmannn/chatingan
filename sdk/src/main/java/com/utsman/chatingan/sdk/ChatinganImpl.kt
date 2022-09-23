package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
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

    private val _sendMessageState = defaultStateEvent<MessageChat>()
    private val _Message_chatStorage = defaultStateEvent<MessageChatStorage>()

    override val config: ChatinganConfig
        get() = chatinganConfig

    private val chatStorage: ChatStorage by lazy {
        ChatStorage(contactStorage, config)
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
        println("ASUUUU contact -> $contact")
        val fcmToken = config.fcmToken
        val contactStore = ContactStore.fromContact(contact, fcmToken)
        return contactStorage.addItem(contactStore, contact.id)
    }

    override suspend fun contacts(): FlowEvent<List<Contact>> {
        return contactStorage.listenItem()
    }

    @OptIn(FlowPreview::class)
    override suspend fun sendMessage(messageChat: MessageChat): FlowEvent<MessageChat> {
        val messageChatStore = MessageChatStore.build(
            senderId = messageChat.senderId,
            receiverId = messageChat.receiverId,
            message = messageChat.messageBody,
            date = messageChat.lastUpdate
        )
        val receiverContact = contactStorage.findItemStoreById(messageChat.receiverId)
        if (receiverContact != null) {
            val receiverName = receiverContact.name
            val receiverToken = receiverContact.token

            val messageRequest = FirebaseMessageRequest.createFromMessage(
                messageChat = messageChat,
                title = receiverName,
                token = receiverToken
            )

            firebaseServices.sendMessage(messageRequest)
                .asFlowEvent()
                .flatMapMerge {
                    getMessageStorage(messageChat)
                }
                .collect {
                    if (it is StateEvent.Success) {
                        val chatInfo = ChatInfo(
                            lastMessage = messageChat.messageBody,
                            readByIds = listOf(messageChat.senderId),
                            lastUpdate = messageChat.lastUpdate
                        )
                        it.invoke()?.addItem(messageChatStore, messageChatStore.id, chatInfo, false)
                    } else {
                        val throwable = it.getExceptionOfNull() ?: Throwable(ErrorMessage.UNKNOWN)
                        val errorState = StateEvent.Failure<MessageChat>(throwable)
                        _sendMessageState.value = errorState
                    }
                }
        } else {
            val throwable = Throwable(ErrorMessage.CONTACT_NOT_FOUND)
            val errorState = StateEvent.Failure<MessageChat>(throwable)
            _sendMessageState.value = errorState
        }

        return _sendMessageState
    }

    override suspend fun sendMessage(
        contact: Contact,
        messageChat: MessageChat
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

        val contacts = listOf(
            config.contact,
            contact
        )

        val chatInfoStorage = ChatInfoStorage(contacts)
        val messageChatStorage = MessageChatStorage(contacts)

        return firebaseServices.sendMessage(messageRequest)
            .asFlowEvent()
            .map {
                it.map {
                    val chatInfo = ChatInfo(
                        lastMessage = messageChat.messageBody,
                        readByIds = listOf(messageChat.senderId),
                        lastUpdate = messageChat.lastUpdate
                    )
                    chatInfo
                }.invoke()
            }
            .filterNotNull()
            .flatMapMerge { chatInfo ->
                chatInfoStorage.addItem(chatInfo.toStore(), chatInfo.id, isMerge = true)
                messageChatStorage.addItem(messageChatStore, messageChatStore.id, isMerge = false)
            }.stateIn(IOScope())
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
        val contacts = listOf(
            config.contact,
            contact
        )
        val messageChatStorage = MessageChatStorage(contacts)
        return messageChatStorage.listenItem()
    }

    override suspend fun getChatInfos(contact: Contact): FlowEvent<List<ChatInfo>> {
        val contacts = listOf(
            config.contact,
            contact
        )
        val chatInfoStorage = ChatInfoStorage(contacts)
        return chatInfoStorage.listenItem()
    }

    override suspend fun getChatInfo(contact: Contact): FlowEvent<ChatInfo> {
        val contacts = listOf(
            config.contact,
            contact
        )
        val chatInfoStorage = ChatInfoStorage(contacts)
        val id = chatInfoStorage.path()
        return chatInfoStorage.findItemByIdFlow(id).stateIn(IOScope())
    }

    override suspend fun getChats(): FlowEvent<List<Chat>> {
        return chatStorage.getChatList()
    }

    override suspend fun getChat(contacts: List<Contact>): FlowEvent<Chat> {
        val messageChatStorage = MessageChatStorage(contacts)
        //return messageChatStorage.getChat()
        return defaultStateEvent()
    }

    override suspend fun markChatRead(contacts: List<Contact>): FlowEvent<ChatInfo> {
       return defaultStateEvent()
    }

    override suspend fun getMessageStorage(contacts: List<Contact>): FlowEvent<MessageChatStorage> {
        val messageChatStorage = MessageChatStorage(contacts)
        _Message_chatStorage.value = StateEvent.Success(messageChatStorage)

        return _Message_chatStorage
    }

    override suspend fun getMessageStorage(messageChat: MessageChat): FlowEvent<MessageChatStorage> {
        _Message_chatStorage.value = StateEvent.Loading()

        val senderId = messageChat.senderId
        val receiverId = messageChat.receiverId

        val sender = contactStorage.findItemById(senderId)
        val receiver = contactStorage.findItemById(receiverId)
        if (sender == null || receiver == null) {
            _Message_chatStorage.value = StateEvent.Failure(Throwable("Contact invalid!"))
        }

        val contacts = listOfNotNull(sender, receiver)
        return getMessageStorage(contacts)
    }


    override suspend fun tokenForId(id: String): FlowEvent<String> {
        return contactStorage.findItemStoreByIdFlow(id).map {
            it.map { data -> data.token }
        }.stateIn(IOScope())
    }


    object ErrorMessage {
        const val CONTACT_NOT_FOUND = "Contact not found!"
        const val UNKNOWN = "Unknown error!"
    }
}