package com.utsman.chatingan.sdk

import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.storage.ChatStorage
import com.utsman.chatingan.sdk.storage.ContactStorage

interface Chatingan {
    val config: ChatinganConfig

    suspend fun addMeContact(contact: Contact, fcmToken: String): FlowEvent<Contact>
    suspend fun contacts(): FlowEvent<List<Contact>>
    suspend fun sendMessage(messageChat: MessageChat): FlowEvent<MessageChat>

    suspend fun createMessageChat(
        senderId: String,
        receiverId: String,
        message: String
    ): MessageChat

    suspend fun getChats(contacts: List<Contact>): FlowEvent<Chat>
    suspend fun tokenForId(id: String): FlowEvent<String>

    suspend fun getChatStorage(messageChat: MessageChat): FlowEvent<ChatStorage>

    companion object {
        @Volatile
        private var instance: Chatingan? = null

        @Volatile
        private var configInstance: ChatinganConfig? = null

        @JvmStatic
        fun setInstance(config: ChatinganConfig) {
            this.configInstance = config
        }

        @JvmStatic
        fun getInstance(): Chatingan =
            instance ?: synchronized(this) {
                val contactStorage = ContactStorage()
                val chatinganConfig = configInstance ?: ChatinganConfig()
                instance ?: ChatinganImpl(
                    contactStorage = contactStorage,
                    chatinganConfig = chatinganConfig
                ).also { instance = it }
            }
    }
}