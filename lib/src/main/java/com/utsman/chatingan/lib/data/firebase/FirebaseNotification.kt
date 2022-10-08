package com.utsman.chatingan.lib.data.firebase

import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.data.model.Message

data class FirebaseNotification(
    var body: String = "",
    var type: NotificationType = NotificationType.MESSAGE,
    var messageType: Message.Type = Message.Type.TEXT,
    var title: String = "",
    var subtitle: String = "",
    var time: Long = Utils.now()
) {
    enum class NotificationType {
        MESSAGE,
        CONTACT_PAIR,
        ASK_CONTACT_PAIR,
        SEND_CONTACT_PAIR
    }

    companion object {
        fun fromMap(map: Map<String, String>): FirebaseNotification {
            return FirebaseNotification(
                body = map["body"].toString(),
                type = map["type"].toString().uppercase().run {
                    NotificationType.valueOf(this)
                },
                messageType = map["messageType"].toString().uppercase().run {
                    Message.Type.valueOf(this)
                },
                title = map["title"].toString(),
                subtitle = map["subtitle"].toString(),
                time = map["time"].toString().toLongOrNull() ?: 0L
            )
        }
    }
}