package com.utsman.chatingan.lib.provider.firebase

import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.network.NotifierResult
import com.utsman.chatingan.lib.data.network.firebase.FirebaseMessageRequest
import com.utsman.chatingan.lib.provider.MessageEmitter
import com.utsman.chatingan.lib.receiver.MessageNotifier

class FirebaseEmitter(
    private val fcmServerKey: String,
    private val logLevel: LogLevel
) : MessageEmitter() {

    private val firebaseWebServices: FirebaseWebServices
        get() = FirebaseWebServices.getInstance(fcmServerKey, logLevel)

    override suspend fun sendNotifier(
        contact: Contact,
        json: String,
        notificationType: MessageNotifier.NotificationType,
        messageType: Message.Type,
        title: String,
        subtitle: String
    ): NotifierResult {
        val firebaseRequest = FirebaseMessageRequest.createFromMessage(token = contact.token) {
            body = json
            type = notificationType

            this.messageType = messageType
            if (title.isNotEmpty()) {
                this.title = title
            }
            if (subtitle.isNotEmpty()) {
                this.subtitle = subtitle
            }
        }

        val response = firebaseWebServices.sendMessage(firebaseRequest)
        if (!response.isSuccessful) return NotifierResult(false, "Internal failure")

        val responseBody = response.body()
        val errorResult = responseBody?.results?.firstOrNull()
            ?.error

        val isSuccess = response.code() == 200 && errorResult == null
        val message = errorResult ?: "Success"

        return NotifierResult(isSuccess, message)
    }

    enum class LogLevel {
        NONE, BASIC, HEADER, BODY
    }
}