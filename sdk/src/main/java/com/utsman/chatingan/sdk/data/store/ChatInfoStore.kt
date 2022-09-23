package com.utsman.chatingan.sdk.data.store

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.type.Store
import java.time.Instant
import java.util.*

class ChatInfoStore(
    var id: String = "",
    var lastMessage: String = "",
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
            id, lastMessage, readByIds, memberIds, lastUpdate
        )
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_LAST_MESSAGE = "lastMessage"
        private const val FIELD_READ_BY_IDS = "readByIds"
        private const val FIELD_MEMBER_IDS = "memberIds"

        @Suppress("UNCHECKED_CAST")
        fun fromMap(data: Map<String, Any>, date: Date): ChatInfoStore {
            return ChatInfoStore(
                id = data[FIELD_ID].toString(),
                lastMessage = data[FIELD_LAST_MESSAGE].toString(),
                readByIds = data[FIELD_READ_BY_IDS] as List<String>,
                memberIds = data[FIELD_MEMBER_IDS] as List<String>,
                lastUpdate = date
            )
        }
    }
}