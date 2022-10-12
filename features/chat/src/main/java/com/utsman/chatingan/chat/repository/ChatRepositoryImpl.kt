package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.collectToStateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ChatRepositoryImpl : ChatRepository {
    override fun getContact(contactId: String): Flow<Contact> {
        return Chatingan.getInstance().getContact(contactId)
    }

    override fun getMessages(scope: CoroutineScope, contact: Contact): FlowEvent<List<Message>> {
        val messages = defaultStateEvent<List<Message>>()
        scope.launch {
            Chatingan.getInstance()
                .getMessages(contact, true)
                .distinctUntilChanged()
                .collectToStateEvent(messages)
        }
        return messages
    }

    override fun setTypingStatus(scope: CoroutineScope, contact: Contact, isTyping: Boolean) {
        scope.launch {
            Chatingan.getInstance()
                .setTyping(contact, isTyping)
        }
    }

    override fun sendMessage(scope: CoroutineScope, contact: Contact, message: Message) {
        scope.launch {
            Chatingan.getInstance()
                .sendMessage(contact, message)
        }
    }

    override fun markAsRead(scope: CoroutineScope, contact: Contact, message: Message) {
        scope.launch {
            Chatingan.getInstance()
                .markMessageIsRead(contact, message)
        }
    }
}