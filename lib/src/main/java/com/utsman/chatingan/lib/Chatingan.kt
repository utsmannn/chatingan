package com.utsman.chatingan.lib

import android.content.Context
import androidx.paging.PagingData
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.impl.ChatinganImpl
import com.utsman.chatingan.lib.preferences.ChatinganPreferences
import com.utsman.chatingan.lib.provider.ImageUploader
import com.utsman.chatingan.lib.provider.MessageEmitter
import com.utsman.chatingan.lib.receiver.MessageNotifier
import kotlinx.coroutines.flow.Flow

interface Chatingan {
    fun getContact(): Contact
    fun getChatinganQr(): ChatinganQr

    fun bindToMessageSubscriber(
        messageNotifier: MessageNotifier,
        onMessageIncoming: (Contact, Message) -> Unit = { _, _ -> }
    )

    fun updateToken(fcmToken: String)
    fun onMessageUpdate(contact: Contact, newMessage: suspend (Message?) -> Unit)

    suspend fun addContact(contact: Contact)
    suspend fun pairContact(contact: Contact)
    fun getAllContact(): Flow<List<Contact>>
    fun getContact(contactId: String): Flow<Contact>
    fun getContactByEmail(email: String): Flow<Contact>

    fun createNewTextMessage(
        contact: Contact,
        textBuilder: Message.MessageTextBuilder.() -> Unit
    ): Message.TextMessages

    suspend fun createNewImageMessage(
        contact: Contact,
        imageBuilder: Message.MessageImageBuilder.() -> Unit
    ): Message.ImageMessages

    suspend fun getMessagesInfo(): Flow<List<MessageInfo>>
    suspend fun sendMessage(contact: Contact, message: Message)

    fun getAllMessages(
        contact: Contact,
        withDivider: Boolean = false,
        asReversed: Boolean = false
    ): Flow<List<Message>>

    fun getAllMessage(
        contact: Contact,
        withDivider: Boolean,
        size: Int = 20,
        isOnlySnapshot: Boolean = true
    ): Flow<PagingData<Message>>

    fun getLastMessage(messageInfo: MessageInfo): Flow<Message>
    suspend fun markMessageIsRead(contact: Contact, message: Message)
    suspend fun setTyping(contact: Contact, isTyping: Boolean)

    companion object {
        @Volatile
        private var chatingan: Chatingan? = null

        data class ChatinganConfigurationBuilder(
            var contact: Contact? = null,
            var freeImageHostApiKey: String = ""
        )

        fun updateContact(context: Context, contact: Contact) {
            ChatinganPreferences.save(context, "contact", contact)
        }

        @JvmStatic
        fun getInstance(): Chatingan {
            return chatingan ?: synchronized(this) {
                chatingan ?: throw ChatinganException("Chatingan not yet initialized!")
            }
        }

        @JvmStatic
        fun getSafeInstance(): Chatingan? {
            return chatingan ?: synchronized(this) {
                chatingan
            }
        }
    }

    class Initializer {
        private lateinit var messageEmitter: MessageEmitter
        private lateinit var imageUploader: ImageUploader

        fun setMessageEmitter(messageEmitter: MessageEmitter): Initializer {
            this.messageEmitter = messageEmitter
            return this
        }

        fun setImageUploader(imageUploader: ImageUploader): Initializer {
            this.imageUploader = imageUploader
            return this
        }

        fun create(context: Context) {
            chatingan = ChatinganImpl(context, messageEmitter, imageUploader)
        }
    }
}