package com.utsman.chatingan.sdk.data

import com.utsman.chatingan.sdk.data.entity.MessageChat

internal data class FirebaseMessageRequest(
    var to: String,
    var data: Notification
) {
    data class Notification(
        var body: String,
        var type: String,
        var title: String,
        var subtitle: String,
        var time: String = System.currentTimeMillis().toString()
    )

    companion object {
        fun createFromMessage(
            messageChat: MessageChat,
            title: String,
            token: String
        ): FirebaseMessageRequest {
            val notification = Notification(
                body = messageChat.messageBody,
                type = "MESSAGE",
                title = title,
                subtitle = messageChat.messageBody
            )
            return FirebaseMessageRequest(
                to = token,
                data = notification
            )
        }
    }
}