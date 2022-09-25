package com.utsman.chatingan.sdk.data.store

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.contract.Store
import java.time.Instant
import java.util.*

class ChatInfoStore(
    var id: String = "",
    var lastMessage: MessageChat = MessageChat(),
    var memberIds: List<String> = emptyList(),
    var readByIds: List<String> = emptyList(),
    var lastUpdate: Date = Date.from(Instant.now())
) : Store {
    override fun toJson(): String {
        val type = object : TypeToken<ChatInfoStore>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }

    fun toChatInfo(): ChatInfo {
        return ChatInfo(
            id = id,
            lastMessage = lastMessage,
            lastUpdate = lastUpdate,
            memberIds = memberIds,
            readByIds = readByIds
        )
    }
}