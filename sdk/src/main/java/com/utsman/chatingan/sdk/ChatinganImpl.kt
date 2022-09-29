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
import com.utsman.chatingan.sdk.data.FirebaseMessageRequest
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.store.ContactStore
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import com.utsman.chatingan.sdk.database.ChatInfoDatabase
import com.utsman.chatingan.sdk.database.ContactDatabase
import com.utsman.chatingan.sdk.database.MessageChatDatabase
import com.utsman.chatingan.sdk.services.FirebaseServices
import com.utsman.chatingan.sdk.utils.DividerCalculator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.time.Instant
import java.util.*

@OptIn(FlowPreview::class)
internal class ChatinganImpl(
    private var _config: ChatinganConfig?
) : Chatingan {

    init {
        addMeContact(config.contact)
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        flowException.value = throwable
    }

    private val flowException: MutableStateFlow<Throwable?> = MutableStateFlow(null)

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
                    state.map { fcmToken }
                }.stateIn(IOScope())
        } else {
            defaultStateEvent()
        }
    }

    private fun addMeContact(contact: Contact): FlowEvent<Contact> {
        val fcmToken = config.fcmToken
        val contactStore = ContactStore.fromContact(contact, fcmToken)
        return contactDatabase.addItem(contactStore, contact.id)
    }

    override suspend fun getContacts(): FlowEvent<List<Contact>> {
        return contactDatabase.listenItem().filterFlow { it.id != config.contact.id }
    }

    override suspend fun sendMessage(
        contact: Contact,
        messageChat: MessageChat
    ): FlowEvent<MessageChat> {
        val pathId = chatInfoIdFinder.getForContact(contact) ?: UUID.randomUUID().toString()
        val messageChatDatabase = MessageChatDatabase(pathId)

        val currentChatInfo = chatInfoDatabase.findItemById(pathId)

        val currentChatInfoId = currentChatInfo?.id
        val currentUnread = currentChatInfo?.unread ?: 0

        val newChatInfo = ChatInfo(
            id = currentChatInfoId ?: pathId,
            lastMessage = messageChat,
            memberIds = listOf(messageChat.senderId, messageChat.receiverId).sorted(),
            unread = currentUnread + 1,
            lastUpdate = messageChat.lastUpdate
        )

        val messageChatStore = messageChat.toStore()

        return chatInfoDatabase.addItem(newChatInfo.toStore(), newChatInfo.id, isMerge = true)
            .flatMapMerge {
                messageChatDatabase.addItem(
                    messageChatStore,
                    messageChatStore.id,
                    isMerge = false
                )
            }.stateIn(IOScope()).apply {
                sendFcm(contact, messageChat)
            }
    }

    private suspend fun sendFcm(contact: Contact, messageChat: MessageChat) {
        contactDatabase.findItemStoreByIdFlow(contact.id)
            .collect {
                val messageRequest = FirebaseMessageRequest.createFromMessage(
                    messageChat = messageChat,
                    title = contact.name,
                    token = it.invoke()?.token.orEmpty()
                )

                try {
                    firebaseServices.sendMessage(messageRequest)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
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
        return messageChatDatabase.listenItem().stateIn(IOScope())
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
        val id = chatInfoIdFinder.getForContact(contact) ?: return emptyStateEvent()
        return chatInfoDatabase.listenItemById(id)
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
                if (it is StateEvent.Success) {
                    if (it.data.isEmpty()) {
                        StateEvent.Empty()
                    } else {
                        it
                    }
                } else {
                    it
                }
            }
            .stateIn(IOScope())
    }

    private suspend fun markChatInfoRead(
        pathId: String,
        messageChat: MessageChat
    ): FlowEvent<ChatInfo> {
        val currentChatInfo = chatInfoDatabase.findItemById(pathId) ?: return emptyStateEvent()
        return if (messageChat.id == currentChatInfo.lastMessage.id) {
            currentChatInfo.lastMessage = messageChat
            currentChatInfo.unread = 0
            chatInfoDatabase.addItem(currentChatInfo.toStore(), pathId, isMerge = false)
        } else {
            errorStateEvent("Chat Info is Empty")
        }.debounce(500).stateIn(IOScope())
    }

    override suspend fun markChatRead(
        contact: Contact,
        messageChat: MessageChat
    ): FlowEvent<ChatInfo> {
        val contactMe = config.contact
        val chatInfoId = chatInfoIdFinder.getForContact(contact) ?: return emptyStateEvent()
        if (messageChat.readByIds.contains(contactMe.id)) return emptyStateEvent()
        if (messageChat.readByIds.size == 2) return emptyStateEvent()

        val messageChatDatabase = MessageChatDatabase(chatInfoId)
        val messageChatStore = messageChat.toStore()
        messageChatStore.readByIds = (messageChat.readByIds + contactMe.id).sorted()
        return messageChatDatabase.addItem(messageChatStore, messageChat.id, isMerge = true)
            .flatMapMerge {
                markChatInfoRead(chatInfoId, messageChatStore.toMessageChat())
            }.stateIn(IOScope())
    }

    override suspend fun sendTypingStatus(contact: Contact, isTyping: Boolean) {
        val chatInfoId = chatInfoIdFinder.getForContact(contact) ?: return
        val chatInfo = chatInfoDatabase.findItemById(chatInfoId) ?: return
        val contactMeId = config.contact.id

        val chatInfoStore = chatInfo.toStore()
        chatInfoStore.typingIds = if (isTyping) {
            (chatInfo.typingIds + contactMeId).distinct()
        } else {
            (chatInfo.typingIds - contactMeId).distinct()
        }

        chatInfoDatabase.addItem(chatInfoStore, chatInfoId, isMerge = true)
    }

    override suspend fun exceptionListener(throwable: (Throwable?) -> Unit) {
        flowException.collect {
            throwable.invoke(it)
        }
    }
}