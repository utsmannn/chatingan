package com.utsman.chatingan.lib.data.model

import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.toDate
import java.util.Date

data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String,
    val fcmToken: String,
    val lastMessageId: String,
    val isTyping: Boolean,
    val lastMessageUpdate: Date,
    val lastUpdate: Date
) {

    data class Builder(
        var id: String = "",
        var name: String = "",
        var email: String = "",
        var imageUrl: String = "",
        var fcmToken: String = ""
    )

    companion object {

        fun empty(): Contact {
            return Contact(
                id = "",
                name = "unknown",
                email = "",
                imageUrl = "",
                fcmToken = "",
                lastMessageId = "",
                isTyping = false,
                lastMessageUpdate = Utils.now().toDate(),
                lastUpdate = Utils.now().toDate()
            )
        }

        fun build(builder: Builder.() -> Unit): Contact {
            val contactBuilder = Builder().apply(builder)
            return Contact(
                id = contactBuilder.id,
                name = contactBuilder.name,
                email = contactBuilder.email,
                imageUrl = contactBuilder.imageUrl,
                fcmToken = contactBuilder.fcmToken,
                lastMessageId = "",
                isTyping = false,
                lastMessageUpdate = Utils.now().toDate(),
                lastUpdate = Utils.now().toDate()
            )
        }
    }
}