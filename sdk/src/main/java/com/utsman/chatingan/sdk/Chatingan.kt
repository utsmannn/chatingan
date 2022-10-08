package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import java.io.File

interface Chatingan {
    val config: ChatinganConfig

    suspend fun updateFcm(fcmToken: String): FlowEvent<String>
    suspend fun getContacts(): FlowEvent<List<Contact>>
    suspend fun sendMessage(contact: Contact, messageChat: MessageChat): FlowEvent<MessageChat>

    suspend fun createMessageTextChat(contact: Contact, message: String): MessageChat
    suspend fun createMessageImageChat(contact: Contact, message: String, file: File): MessageChat

    suspend fun getMessages(contact: Contact): FlowEvent<List<MessageChat>>
    suspend fun getChat(contact: Contact): FlowEvent<Chat>
    suspend fun getChatInfos(): FlowEvent<List<ChatInfo>>
    suspend fun getChatInfo(contact: Contact): FlowEvent<ChatInfo>
    suspend fun getContact(id: String): FlowEvent<Contact>

    suspend fun getChats(): FlowEvent<List<Chat>>
    suspend fun markChatRead(contact: Contact, messageChat: MessageChat): FlowEvent<ChatInfo>
    suspend fun sendTypingStatus(contact: Contact, isTyping: Boolean)

    suspend fun exceptionListener(throwable: (Throwable?) -> Unit)

    companion object {
        @Volatile
        private var instance: Chatingan? = null

        @Volatile
        private var config: ChatinganConfig? = null

        @JvmStatic
        fun initialize(config: ChatinganConfig) {
            this.config = config
        }

        @JvmStatic
        fun getInstance(): Chatingan =
            instance ?: synchronized(this) {
                instance ?: ChatinganImpl(config).also { instance = it }
            }
    }
}