package com.utsman.chatingan.sdk.data.entity

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.store.ChatInfoStore
import com.utsman.chatingan.sdk.data.contract.Entity
import com.utsman.chatingan.sdk.data.contract.JsonParcelize
import java.time.Instant
import java.util.*

data class ChatInfo(
    var id: String = "",
    var lastMessage: MessageChat = MessageChat(),
    var memberIds: List<String> = emptyList(),
    var readByIds: List<String> = emptyList(),
    var lastUpdate: Date = Date.from(Instant.now())
) : Entity, JsonParcelize {

    fun toStore(): ChatInfoStore {
        return ChatInfoStore(
            id, lastMessage, memberIds, readByIds, lastUpdate
        )
    }

    fun isNotRead(contact: Contact): Boolean {
        return readByIds.size == 1 && readByIds.contains(contact.id)
    }

    fun isReadFromReceiver(contact: Contact): Boolean {
        return readByIds.contains(contact.id)
    }

    fun isDelivered(contact: Contact): Boolean {
        return readByIds.contains(contact.id)
    }

    fun isFromMe(contact: Contact): Boolean {
        return lastMessage.receiverId == contact.id
    }

    override fun toJson(): String {
        val type = object : TypeToken<ChatInfo>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }
}