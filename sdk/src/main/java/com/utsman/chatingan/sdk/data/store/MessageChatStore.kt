package com.utsman.chatingan.sdk.data.store

import android.net.Uri
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.data.type.Store
import java.time.Instant
import java.util.*

data class MessageChatStore(
    var id: String = "",
    var senderId: String = "",
    var receiverId: String = "",
    var messageBody: String = "",
    @ServerTimestamp
    var lastUpdate: Date = Date.from(Instant.now())
) : Store {

    fun toMessageChat(): MessageChat {
        return MessageChat(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            messageBody = messageBody,
            lastUpdate = lastUpdate
        )
    }

    companion object {
        private const val FIELD_ID = "id"
        private const val FIELD_SENDER_ID = "senderId"
        private const val FIELD_RECEIVER_ID = "receiverId"
        private const val FIELD_MESSAGE_BODY = "messageBody"

        fun build(senderId: String, receiverId: String, message: String, date: Date): MessageChatStore {
            return MessageChatStore(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                messageBody = message,
                lastUpdate = date
            )
        }

        fun fromMap(data: Map<String, Any>, date: Date): MessageChatStore {
            return MessageChatStore(
                id = data[FIELD_ID].toString(),
                senderId = data[FIELD_SENDER_ID].toString(),
                receiverId = data[FIELD_RECEIVER_ID].toString(),
                messageBody = data[FIELD_MESSAGE_BODY].toString(),
                lastUpdate = date
            )
        }
    }

    override fun toJson(): String {
        val type = object : TypeToken<MessageChatStore>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }
}