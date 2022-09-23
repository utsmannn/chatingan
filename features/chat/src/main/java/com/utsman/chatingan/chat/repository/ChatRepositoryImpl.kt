package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat

class ChatRepositoryImpl : ChatRepository {

    private val _chatState = defaultStateEvent<Chat>()
    override val chatState: FlowEvent<Chat>
        get() = _chatState

    private val _message = defaultStateEvent<MessageChat>()
    override val message: FlowEvent<MessageChat>
        get() = _message

    override suspend fun getChat(contact: Contact) {
        val chatingan = Chatingan.getInstance()
        val contactMe = chatingan.config.contact
        val contacts = listOf(contactMe, contact)

        Chatingan.getInstance()
            .getChat(contacts)
            .collect(_chatState)
            /*.collect {
                if (it.invoke() != _chatState.value.invoke()) {
                    _chatState.value = it
                }
            }*/
    }

    override suspend fun readChat(contact: Contact) {
        val chatingan = Chatingan.getInstance()
        val contactMe = chatingan.config.contact
        val contacts = listOf(contactMe, contact)

        Chatingan.getInstance()
            .markChatRead(contacts)
    }

    override suspend fun sendMessage(receiverId: String, message: String) {
        val chatingan = Chatingan.getInstance()
        val senderId = chatingan.config.contact.id
        val messageChat = chatingan.createMessageChat(senderId, receiverId, message)
        chatingan.sendMessage(messageChat).collect(_message)
    }

    override suspend fun sendMessage(contact: Contact, message: String) {

    }
}