package com.utsman.chatingan.sdk.data.store

import android.net.Uri
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.type.Store
import java.time.Instant
import java.util.*

data class ContactStore(
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var token: String = "",
    @ServerTimestamp
    var lastUpdate: Date = Date.from(Instant.now())
) : Store {

    fun toContact(): Contact {
        return Contact(
            id = id,
            name = name,
            image = image,
            lastUpdate = lastUpdate
        )
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_IMAGE = "image"
        private const val FIELD_TOKEN = "token"

        fun fromMap(data: Map<String, Any>, date: Date): ContactStore {
            return ContactStore(
                id = data[FIELD_ID].toString(),
                name = data[FIELD_NAME].toString(),
                image = data[FIELD_IMAGE].toString(),
                token = data[FIELD_TOKEN].toString(),
                lastUpdate = date
            )
        }

        fun fromContact(contact: Contact, fcmToken: String): ContactStore {
            return  ContactStore(
                id = contact.id,
                name = contact.name,
                image = contact.image,
                token = fcmToken
            )
        }
    }

    override fun toJson(): String {
        val type = object : TypeToken<ContactStore>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }
}