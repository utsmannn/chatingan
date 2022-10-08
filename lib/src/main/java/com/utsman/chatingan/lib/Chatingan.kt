package com.utsman.chatingan.lib

import android.content.Context
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import kotlinx.coroutines.flow.Flow

interface Chatingan {
    fun getConfiguration(): ChatinganConfiguration
    fun getChatinganQr(): ChatinganQr

    fun bindToFirebaseMessagingServices(data: Map<String, String>)

    suspend fun addContact(contact: Contact)
    suspend fun pairContact(contact: Contact)
    suspend fun getAllContact(): Flow<List<Contact>>
    suspend fun getContactByEmail(email: String): Flow<Contact>

    suspend fun getMessagesInfo(): Flow<List<MessageInfo>>

    suspend fun sendMessage(message: Message)
    suspend fun getMessages(messageInfo: MessageInfo): Flow<List<Message>>
    suspend fun getLastMessage(messageInfo: MessageInfo): Flow<Message>

    companion object {
        @Volatile
        private var chatingan: Chatingan? = null

        data class ChatinganConfigurationBuilder(
            var contact: Contact? = null,
            var fcmToken: String = "",
            var fcmServerKey: String = ""
        )

        fun initialize(context: Context, configuration: ChatinganConfigurationBuilder.() -> Unit) {
            val currentConfigBuilder = ChatinganConfigurationBuilder().apply(configuration)
            val currentConfiguration = ChatinganConfiguration(
                contact = currentConfigBuilder.contact
                    ?: throw ChatinganException("Contact not found!"),
                fcmToken = currentConfigBuilder.fcmToken,
                fcmServerKey = currentConfigBuilder.fcmServerKey
            )

            chatingan = ChatinganImpl(context, currentConfiguration)
        }

        @JvmStatic
        fun getInstance(): Chatingan {
            return chatingan ?: synchronized(this) {
                chatingan ?: throw ChatinganException("Chatingan not yet initialized!")
            }
        }
    }
}