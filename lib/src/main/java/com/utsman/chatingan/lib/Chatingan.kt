package com.utsman.chatingan.lib

import android.content.Context
import androidx.paging.PagingData
import com.utsman.chatingan.lib.configuration.ChatinganConfiguration
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.impl.ChatinganImpl
import kotlinx.coroutines.flow.Flow

interface Chatingan {
    fun getConfiguration(): ChatinganConfiguration
    fun getChatinganQr(): ChatinganQr

    fun bindToFirebaseMessagingServices(
        data: Map<String, String>,
        onMessageIncoming: (Contact, Message) -> Unit = { _, _ -> }
    )

    fun updateFcmToken(fcmToken: String)
    suspend fun onMessageUpdate(contact: Contact, newMessage: suspend (Message?) -> Unit)

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
            var fcmServerKey: String = "",
            var freeImageHostApiKey: String = ""
        )

        fun initialize(context: Context, configuration: ChatinganConfigurationBuilder.() -> Unit) {
            val configBuilder = ChatinganConfigurationBuilder().apply(configuration)
            val config = ChatinganConfiguration(
                fcmServerKey = configBuilder.fcmServerKey,
                freeImageHostApiKey = configBuilder.freeImageHostApiKey
            ).also {
                val contact = configBuilder.contact
                if (contact != null) {
                    it.updateContact(contact)
                }
            }

            config.savePref(context)
            chatingan = ChatinganImpl(context)
        }

        fun updateConfig(
            context: Context,
            configuration: ChatinganConfigurationBuilder.() -> Unit
        ) {
            val configBuilder = ChatinganConfigurationBuilder().apply(configuration)
            val currentConfig = ChatinganConfiguration.getPref(context)

            val newServerKey = configBuilder.fcmServerKey.ifEmpty {
                currentConfig.fcmServerKey
            }

            val newImgBBApiKey = configBuilder.freeImageHostApiKey.ifEmpty {
                currentConfig.freeImageHostApiKey
            }

            val newConfig = ChatinganConfiguration(newServerKey, newImgBBApiKey)
                .also {
                    val newContact = configBuilder.contact
                    if (newContact != null) {
                        it.updateContact(newContact)
                    }
                }

            newConfig.savePref(context)
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

        /*fun setReceiver(chatinganReceiver: ChatinganReceiver) {
            receiver = chatinganReceiver
        }*/
    }
}