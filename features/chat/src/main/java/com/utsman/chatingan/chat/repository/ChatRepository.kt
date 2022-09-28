package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import org.koin.core.module.Module
import org.koin.dsl.module

interface ChatRepository {
    val chatState: FlowEvent<Chat>
    val message: FlowEvent<MessageChat>

    suspend fun getChat(contact: Contact)
    suspend fun readChat(contact: Contact, messageChat: MessageChat)
    suspend fun sendMessage(contact: Contact, message: String)
    suspend fun setTypingStatus(contact: Contact, isTyping: Boolean)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ChatRepository> { ChatRepositoryImpl() }
            }
        }
    }
}