package com.utsman.chatingan.lib.data.network.firebase

import com.utsman.chatingan.lib.receiver.MessageNotifier

internal data class FirebaseMessageRequest(
    var to: String,
    var data: MessageNotifier
) {

    companion object {
        fun createFromMessage(
            token: String,
            notification: MessageNotifier.() -> Unit
        ): FirebaseMessageRequest {
            val currentNotification = MessageNotifier().apply(notification)
            return FirebaseMessageRequest(
                to = token,
                data = currentNotification
            )
        }
    }
}