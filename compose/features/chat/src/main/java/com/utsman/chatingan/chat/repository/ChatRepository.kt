package com.utsman.chatingan.chat.repository

import androidx.paging.PagingData
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module
import org.koin.dsl.module

interface ChatRepository {
    fun getContact(contactId: String): Flow<Contact>
    fun getMessages(scope: CoroutineScope, contact: Contact): Flow<PagingData<Message>>
    fun setTypingStatus(scope: CoroutineScope, contact: Contact, isTyping: Boolean)
    fun sendMessage(scope: CoroutineScope, contact: Contact, message: Message)

    fun markAsRead(scope: CoroutineScope, contact: Contact, message: Message)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ChatRepository> { ChatRepositoryImpl() }
            }
        }
    }
}