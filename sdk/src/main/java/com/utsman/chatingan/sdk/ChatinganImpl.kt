package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.IOScope
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.emptyStateEvent
import com.utsman.chatingan.common.event.errorStateEvent
import com.utsman.chatingan.common.event.filterFlow
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
import com.utsman.chatingan.sdk.database.ChatInfoDatabase
import com.utsman.chatingan.sdk.database.ContactDatabase
import com.utsman.chatingan.sdk.database.MessageChatDatabase
import com.utsman.chatingan.sdk.utils.DividerCalculator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

@OptIn(FlowPreview::class)
internal class ChatinganImpl(
    private var _config: ChatinganConfig?
) : Chatingan {

    init {
        IOScope().launch {
            addMeContact(config.contact)
        }
    }

    override val config: ChatinganConfig
        get() = _config ?: throw IllegalStateException("Chatingan not initialized!")

    private val firebaseServices: FirebaseServices
        get() = FirebaseServices.instance(config)

    private val contactDatabase: ContactDatabase
        get() = ContactDatabase()

    private val chatInfoDatabase: ChatInfoDatabase
        get() = ChatInfoDatabase()

    private val chatInfoIdFinder: ChatInfoDatabase.IdFinder
        get() = ChatInfoDatabase.IdFinder(config)

    override suspend fun updateFcm(fcmToken: String): FlowEvent<String> {
        val currentContact = config.contact
        return if (currentContact.id.isNotEmpty()) {
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
        return contactDatabase.addItem(contactStore, contact.id)
    }

    override suspend fun getContacts(): FlowEvent<List<Contact>> {
        return contactDatabase.listenItem().filterFlow { it.id != config.contact.id }
    }

    override suspend fun sendMessage(
        contact: Contact,
        messageChat: MessageChat,
        chatInfo: ChatInfo?
    ): FlowEvent<MessageChat> {
        val pathId = chatInfoIdFinder.getForContact(contact) ?: UUID.randomUUID().toString()
        val messageChatDatabase = MessageChatDatabase(pathId)

        val currentChatInfo = chatInfoDatabase.findItemById(pathId)

        val currentChatInfoId = currentChatInfo?.id

        val newChatInfo = ChatInfo(
            id = currentChatInfoId ?: pathId,
            lastMessage = messageChat,
            memberIds = listOf(messageChat.senderId, messageChat.receiverId).sorted(),
            lastUpdate = messageChat.lastUpdate
        )

        val messageChatStore = messageChat.toStore()

        return contactDatabase.findItemStoreByIdFlow(contact.id)
            .flatMapMerge {
                val messageRequest = FirebaseMessageRequest.createFromMessage(
                    messageChat = messageChat,
                    title = contact.name,
                    token = it.invoke()?.token.orEmpty()
                )
                firebaseServices.sendMessage(messageRequest)
                    .asFlowEvent()
                    .flatMapMerge {
                        chatInfoDatabase.addItem(newChatInfo.toStore(), newChatInfo.id, isMerge = true)
                            .flatMapMerge {
                                messageChatDatabase.addItem(
                                    messageChatStore,
                                    messageChatStore.id,
                                    isMerge = false
                                )
                            }
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
        val messageChatDatabase = MessageChatDatabase(pathId)
        return messageChatDatabase.listenItem().debounce(500).stateIn(IOScope())
    }

    override suspend fun getChat(contact: Contact): FlowEvent<Chat> {
        var chatInfo = ChatInfo()
        return chatInfoIdFinder.listenForContact(contact)
            .map {
                it.map { pathId ->
                    chatInfo = chatInfoDatabase.findItemById(pathId) ?: ChatInfo()
                    MessageChatDatabase(pathId)
                }

            }.flatMapMerge {
                val storage = it.invoke()
                storage?.listenItem() ?: emptyStateEvent()
            }
            .debounce(500)
            .map {
                it.map { messages ->
                    val newMessages = DividerCalculator.calculateDividerChat(messages)
                    Chat(
                        id = chatInfo.id,
                        contact = contact,
                        messages = newMessages,
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

    override suspend fun getChatInfos(): FlowEvent<List<ChatInfo>> {
        return chatInfoDatabase.listenItem()
    }

    override suspend fun getChatInfo(contact: Contact): FlowEvent<ChatInfo> {
        val id = chatInfoDatabase.path()
        return chatInfoDatabase.findItemByIdFlow(id).stateIn(IOScope())
    }

    override suspend fun getContact(id: String): FlowEvent<Contact> {
        return contactDatabase.findItemByIdFlow(id).stateIn(IOScope())
    }

    @Suppress("LABEL_NAME_CLASH")
    override suspend fun getChats(): FlowEvent<List<Chat>> {
        return chatInfoDatabase.listenItem()
            .map { state ->
                state.map { infos ->
                    infos
                        .filter {
                            it.memberIds.contains(config.contact.id)
                        }
                        .map { info ->
                            val member = info.memberIds
                                .find { it != config.contact.id } ?: return@map null
                            val contact = contactDatabase.findItemById(member) ?: return@map null
                            Chat(
                                id = info.id,
                                contact = contact,
                                messages = emptyList(),
                                chatInfo = info
                            )

                        }
                        .filterNotNull()
                        .asReversed()
                }
            }
            .filterFlow {
                it.id.isNotEmpty()
            }
            .map {
                if (it.invoke().isNullOrEmpty()) {
                    StateEvent.Empty()
                } else {
                    it
                }
            }
            .debounce(500)
            .stateIn(IOScope())
    }

    private suspend fun markChatInfoRead(
        pathId: String,
        messageChat: MessageChat
    ): FlowEvent<ChatInfo> {
        val currentChatInfo = chatInfoDatabase.findItemById(pathId) ?: return emptyStateEvent()
        return if (messageChat.id == currentChatInfo.lastMessage.id) {
            currentChatInfo.lastMessage = messageChat
            chatInfoDatabase.addItem(currentChatInfo.toStore(), pathId, isMerge = false)
        } else {
            errorStateEvent("Chat Info is Empty")
        }.debounce(500).stateIn(IOScope())
    }

    override suspend fun markChatRead(
        chatInfo: ChatInfo,
        messageChat: MessageChat
    ): FlowEvent<ChatInfo> {
        val contactMe = config.contact
        if (messageChat.readByIds.contains(contactMe.id)) return emptyStateEvent()
        if (messageChat.readByIds.size == 2) return emptyStateEvent()

        val messageChatDatabase = MessageChatDatabase(chatInfo.id)
        val messageChatStore = messageChat.toStore()
        messageChatStore.readByIds = (messageChat.readByIds + contactMe.id).sorted()
        return messageChatDatabase.addItem(messageChatStore, messageChat.id, isMerge = true)
            .flatMapMerge {
                markChatInfoRead(chatInfo.id, messageChatStore.toMessageChat())
            }.stateIn(IOScope())
    }
}