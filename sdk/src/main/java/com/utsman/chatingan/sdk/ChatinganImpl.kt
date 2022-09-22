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
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.store.ContactStore
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import com.utsman.chatingan.sdk.services.FirebaseServices
import com.utsman.chatingan.sdk.storage.ChatStorage
import com.utsman.chatingan.sdk.storage.ContactStorage
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ChatinganImpl(
    private val contactStorage: ContactStorage,
    private val chatinganConfig: ChatinganConfig,
) : Chatingan {

    private val firebaseServices: FirebaseServices by lazy {
        FirebaseServices.instance(chatinganConfig)
    }

    private val _sendMessageState = defaultStateEvent<MessageChat>()
    private val _chatStorage = defaultStateEvent<ChatStorage>()

    override val config: ChatinganConfig
        get() = chatinganConfig

    override suspend fun addMeContact(contact: Contact, fcmToken: String): FlowEvent<Contact> {
        val contactStore = ContactStore.fromContact(contact, fcmToken)
        return contactStorage.addItem(contactStore, contact.id)
    }

    override suspend fun contacts(): FlowEvent<List<Contact>> {
        return contactStorage.listenItem()
    }

    override suspend fun sendMessage(messageChat: MessageChat): FlowEvent<MessageChat> {
        val messageChatStore = MessageChatStore.build(
            senderId = messageChat.senderId,
            receiverId = messageChat.receiverId,
            message = messageChat.messageBody
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
                    getChatStorage(messageChat)
                }
                .collect {
                    if (it is StateEvent.Success) {
                        it.invoke()?.addItem(messageChatStore, messageChatStore.id)
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

    override suspend fun createMessageChat(
        senderId: String,
        receiverId: String,
        message: String
    ): MessageChat {
        return MessageChatStore.build(
            senderId = senderId,
            receiverId = receiverId,
            message = message
        ).toMessageChat()
    }

    override suspend fun getChats(contacts: List<Contact>): FlowEvent<Chat> {
        println("ASUUUU get chat..")
        val chatStorage = ChatStorage(contacts)
        return chatStorage.getChat()
    }

    override suspend fun getChatStorage(messageChat: MessageChat): FlowEvent<ChatStorage> {
        _chatStorage.value = StateEvent.Loading()

        val senderId = messageChat.senderId
        val receiverId = messageChat.receiverId

        val sender = contactStorage.findItemById(senderId)
        val receiver = contactStorage.findItemById(receiverId)
        if (sender == null || receiver == null) {
            _chatStorage.value = StateEvent.Failure(Throwable("Contact invalid!"))
        }

        val contacts = listOfNotNull(sender, receiver)
        val chatStorage = ChatStorage(contacts)
        _chatStorage.value = StateEvent.Success(chatStorage)

        return _chatStorage
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