package com.utsman.chatingan.lib.receiver

import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.data.model.Message

data class MessageNotifier(
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
        TYPING,
        MESSAGE_REPORT,
        CONTACT_UPDATE
    }

    companion object {
        fun fromMap(map: Map<String, String>): MessageNotifier {
            return MessageNotifier(
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