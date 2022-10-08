package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.loadingEventValue
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ChatRepositoryImpl : ChatRepository {

    private val _chatState = defaultStateEvent<Chat>()
    override val chatState: FlowEvent<Chat>
        get() = _chatState

    private val _chatInfoState = defaultStateEvent<ChatInfo>()
    override val chatInfoState: FlowEvent<ChatInfo>
        get() = _chatInfoState

    private val _message = defaultStateEvent<MessageChat>()
    override val message: FlowEvent<MessageChat>
        get() = _message

    private val _isReceiverIsTyping = MutableStateFlow(false)
    override val isReceiverIsTyping: StateFlow<Boolean>
        get() = _isReceiverIsTyping

    override suspend fun getChat(contact: Contact) {
        _chatState.value = loadingEventValue()
        Chatingan.getInstance()
            .getChat(contact)
            .collect(_chatState)
    }

    override suspend fun getChatInfo(contact: Contact) {
        _chatInfoState.value = loadingEventValue()
        Chatingan.getInstance()
            .getChatInfo(contact)
            .collect {
                _chatInfoState.value = it
                if (it is StateEvent.Success) {
                    val contactId = contact.id
                    val isContactTyping = it.data.typingIds.contains(contactId)
                    _isReceiverIsTyping.value = isContactTyping
                }
            }
    }

    override suspend fun readChat(contact: Contact, messageChat: MessageChat) {
        val chatingan = Chatingan.getInstance()
        chatingan.markChatRead(contact, messageChat)
    }

    override suspend fun sendMessage(contact: Contact, message: String) {
        _message.value = loadingEventValue()
        val chatingan = Chatingan.getInstance()
        val messageChat = chatingan.createMessageTextChat(contact, message)
        chatingan.sendMessage(contact, messageChat).collect(_message)
    }

    override suspend fun sendImage(contact: Contact, message: String, file: File) {
        _message.value = loadingEventValue()
        val chatingan = Chatingan.getInstance()
        val messageChat = chatingan.createMessageImageChat(contact, message, file)
        chatingan.sendMessage(contact, messageChat).collect(_message)
    }

    override suspend fun setTypingStatus(contact: Contact, isTyping: Boolean) {
        Chatingan.getInstance().sendTypingStatus(contact, isTyping)
    }

    override suspend fun dispose() {
        _chatState.value = StateEvent.Idle()
        _chatInfoState.value = StateEvent.Idle()
        _message.value = StateEvent.Idle()
        _isReceiverIsTyping.value = false
    }
}