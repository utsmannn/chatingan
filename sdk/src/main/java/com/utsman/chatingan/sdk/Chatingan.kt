package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.storage.MessageChatStorage
import com.utsman.chatingan.sdk.storage.ContactStorage

interface Chatingan {
    val config: ChatinganConfig

    suspend fun updateFcm(fcmToken: String): FlowEvent<String>
    suspend fun addMeContact(contact: Contact): FlowEvent<Contact>
    suspend fun getContacts(): FlowEvent<List<Contact>>
    suspend fun sendMessage(contact: Contact, messageChat: MessageChat, chatInfo: ChatInfo?): FlowEvent<MessageChat>

    suspend fun createMessageChat(
        senderId: String,
        receiverId: String,
        message: String
    ): MessageChat

    suspend fun getMessages(contact: Contact): FlowEvent<List<MessageChat>>
    suspend fun getChat(contact: Contact): FlowEvent<Chat>
    suspend fun getChatInfos(): FlowEvent<List<ChatInfo>>
    suspend fun getChatInfo(contact: Contact): FlowEvent<ChatInfo>
    suspend fun getContact(id: String): FlowEvent<Contact>

    suspend fun getChats(): FlowEvent<List<Chat>>
    suspend fun markChatRead(chatInfo: ChatInfo, messageChat: MessageChat): FlowEvent<ChatInfo>

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