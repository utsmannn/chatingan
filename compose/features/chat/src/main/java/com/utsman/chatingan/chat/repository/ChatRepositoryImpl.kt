package com.utsman.chatingan.chat.repository

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class ChatRepositoryImpl : ChatRepository {
    override fun getContact(contactId: String): Flow<Contact> {
        return Chatingan.getInstance().getContact(contactId)
    }

    override fun getMessages(
        scope: CoroutineScope,
        contact: Contact
    ): Flow<PagingData<Message>> {
        val newScope = scope + Dispatchers.IO
        return Chatingan
            .getInstance()
            .getAllMessage(contact, true, 10, false)
            .distinctUntilChanged()
            .cachedIn(newScope)
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