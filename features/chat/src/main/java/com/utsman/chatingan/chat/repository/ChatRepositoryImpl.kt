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
import kotlinx.coroutines.flow.onEach

class ChatRepositoryImpl : ChatRepository {

    private val _chatState = defaultStateEvent<Chat>()
    override val chatState: FlowEvent<Chat>
        get() = _chatState

    private val _message = defaultStateEvent<MessageChat>()
    override val message: FlowEvent<MessageChat>
        get() = _message

    override suspend fun getChat(contact: Contact) {
        _chatState.value = loadingEventValue()
        Chatingan.getInstance()
            .getChat(contact)
            .collect(_chatState)
    }

    override suspend fun readChat(chatInfo: ChatInfo, messageChat: MessageChat) {
        val chatingan = Chatingan.getInstance()
        chatingan.markChatRead(chatInfo, messageChat)
    }

    override suspend fun sendMessage(contact: Contact, message: String, chatInfo: ChatInfo?) {
        _message.value = loadingEventValue()
        val chatingan = Chatingan.getInstance()
        val senderId = chatingan.config.contact.id
        val messageChat = chatingan.createMessageChat(senderId, contact.id, message)
        chatingan.sendMessage(contact, messageChat, chatInfo).collect(_message)
    }
}