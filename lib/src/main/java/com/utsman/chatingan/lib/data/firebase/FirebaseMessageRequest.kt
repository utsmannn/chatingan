package com.utsman.chatingan.lib.data.firebase

internal data class FirebaseMessageRequest(
    var to: String,
    var data: FirebaseNotification
) {

    companion object {
        fun createFromMessage(
            token: String,
            notification: FirebaseNotification.() -> Unit
        ): FirebaseMessageRequest {
            val currentNotification = FirebaseNotification().apply(notification)
            return FirebaseMessageRequest(
                to = token,
                data = currentNotification
            )
        }
    }
}