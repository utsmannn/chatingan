package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.base.BaseRepository
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

interface ChatRepository : BaseRepository {
    val chatState: FlowEvent<Chat>
    val chatInfoState: FlowEvent<ChatInfo>
    val message: FlowEvent<MessageChat>
    val isReceiverIsTyping: StateFlow<Boolean>

    suspend fun getChat(contact: Contact)
    suspend fun getChatInfo(contact: Contact)
    suspend fun readChat(contact: Contact, messageChat: MessageChat)
    suspend fun sendMessage(contact: Contact, message: String)
    suspend fun sendImage(contact: Contact, message: String, file: File)
    suspend fun setTypingStatus(contact: Contact, isTyping: Boolean)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ChatRepository> { ChatRepositoryImpl() }
            }
        }
    }
}