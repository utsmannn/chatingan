package com.utsman.chatingan.lib.data.model

import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.toDate
import java.util.Date
import java.util.UUID

data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String,
    val fcmToken: String,
    val lastUpdate: Date
) {

    data class Builder(
        var name: String = "",
        var email: String = "",
        var imageUrl: String = "",
        var fcmToken: String = ""
    )

    companion object {

        fun empty(): Contact {
            return Contact(
                "", "unknown", "", "", "", Utils.now().toDate()
            )
        }

        fun build(builder: Builder.() -> Unit): Contact {
            val contactBuilder = Builder().apply(builder)
            return Contact(
                id = UUID.randomUUID().toString(),
                name = contactBuilder.name,
                email = contactBuilder.email,
                imageUrl = contactBuilder.imageUrl,
                fcmToken = contactBuilder.fcmToken,
                lastUpdate = Utils.now().toDate()
            )
        }
    }
}