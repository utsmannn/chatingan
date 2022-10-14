package com.utsman.chatingan.lib.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.toDate
import java.util.Date
import java.util.UUID

/**
 * @param superDate use in divider message
 * Why not using inherited param? Caused error in json parse (multiple field detected)
 * */
sealed class Message(val superDate: Date) {
    data class TextMessages(
        val id: String,
        val senderId: String,
        val receiverId: String,
        val status: Status,
        val messageBody: String,
        val date: Date
    ) : Message(date)

    data class ImageMessages(
        val id: String,
        val senderId: String,
        val receiverId: String,
        val status: Status,
        val imageBody: MessageImageBody,
        val date: Date
    ) : Message(date)

    data class DividerMessage(
        val date: Date,
        val message: Message
    ) : Message(date)

    enum class Type {
        TEXT, IMAGE, OTHER
    }

    enum class Status {
        SENDING, SENT, RECEIVED, READ, FAILURE, NONE
    }

    data class MessageTextBuilder(
        var message: String = ""
    )

    data class MessageImageBody(
        var imageUrl: String = "",
        var thumbUrl: String = "",
        var caption: String = ""
    ) {
        companion object {
            private val type = object : TypeToken<MessageImageBody>() {}.type
            fun fromStringBody(body: String): MessageImageBody {
                return Gson().fromJson(body, type)
            }
        }

        fun toStringBody(): String {
            return Gson().toJson(this, type)
        }
    }

    companion object {

        fun buildTextMessage(
            contact: Contact,
            textBuilder: MessageTextBuilder.() -> Unit
        ): TextMessages {
            val messageTextBuilder = MessageTextBuilder().apply(textBuilder)
            val currentChatingan = Chatingan.getInstance()
            val currentConfig = currentChatingan.getConfiguration()

            return TextMessages(
                id = UUID.randomUUID().toString(),
                senderId = currentConfig.contact.id,
                receiverId = contact.id,
                status = Status.SENDING,
                messageBody = messageTextBuilder.message,
                date = Utils.now().toDate()
            )
        }

        fun emptyTextMessage(): TextMessages {
            return TextMessages("", "", "", Status.FAILURE, "", Utils.now().toDate())
        }
    }


    fun isNotEmpty(): Boolean {
        return when (this) {
            is TextMessages -> id.isNotEmpty()
            is ImageMessages -> id.isNotEmpty()
            is DividerMessage -> false
        }
    }

    fun updateStatus(status: Status): Message {
        return when (this) {
            is TextMessages -> copy(status = status)
            is ImageMessages -> copy(status = status)
            is DividerMessage -> this
        }
    }

    fun isStatus(status: Status): Boolean {
        return when (this) {
            is TextMessages -> {
                this.status == status
            }
            is ImageMessages -> {
                this.status == status
            }
            is DividerMessage -> false
        }
    }

    fun getChildId(): String {
        return when (this) {
            is TextMessages -> {
                this.id
            }
            is ImageMessages -> {
                this.id
            }
            is DividerMessage -> {
                this.message.getChildId()
            }
        }
    }

    fun getChildReceiverId(): String {
        return when (this) {
            is TextMessages -> {
                this.receiverId
            }
            is ImageMessages -> {
                this.receiverId
            }
            is DividerMessage -> "unknown"
        }
    }

    fun getChildSenderId(): String {
        return when (this) {
            is TextMessages -> {
                this.senderId
            }
            is ImageMessages -> {
                this.senderId
            }
            is DividerMessage -> "unknown"
        }
    }

    fun getChildStatus(): Status {
        return when (this) {
            is TextMessages -> {
                this.status
            }
            is ImageMessages -> {
                this.status
            }
            is DividerMessage -> Status.NONE
        }
    }
}