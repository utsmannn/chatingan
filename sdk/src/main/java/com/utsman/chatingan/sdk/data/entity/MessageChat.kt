package com.utsman.chatingan.sdk.data.entity

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.store.MessageChatStore
import com.utsman.chatingan.sdk.data.contract.Entity
import java.time.Instant
import java.util.*

data class MessageChat(
    var id: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var messageBody: String = "",
    var lastUpdate: Date = Date.from(Instant.now())
) : Entity {
    override fun toJson(): String {
        val type = object : TypeToken<MessageChat>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }

    internal fun toStore(): MessageChatStore {
        return MessageChatStore(
            id, senderId, receiverId, messageBody, lastUpdate
        )
    }
}