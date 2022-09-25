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
    var detail: Contact.Detail = Contact.Detail(),
    @ServerTimestamp
    var lastUpdate: Date = Date.from(Instant.now())
) : Store {

    fun toContact(): Contact {
        return Contact(
            id = id,
            name = name,
            image = image,
            detail = detail,
            lastUpdate = lastUpdate
        )
    }

    companion object {
        fun fromContact(contact: Contact, fcmToken: String): ContactStore {
            return  ContactStore(
                id = contact.id,
                name = contact.name,
                image = contact.image,
                detail = contact.detail,
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