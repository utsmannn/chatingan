package com.utsman.chatingan.lib.data.model

import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.toDate
import java.util.Date
import java.util.UUID

sealed class Message {
    data class TextMessages(
        val id: String,
        val messageInfoId: String,
        val senderId: String,
        val receiverId: String,
        val status: Status,
        val messageBody: String,
        val date: Date
    ) : Message()

    data class ImageMessages(
        val id: String,
        val messageInfoId: String,
        val senderId: String,
        val receiverId: String,
        val status: Status,
        val caption: String,
        val imageUrl: String,
        val thumbUrl: String,
        val date: Date
    ) : Message()

    enum class Type {
        TEXT, IMAGE, OTHER
    }

    enum class Status {
        SENDING, SENT, RECEIVED, READ, FAILURE
    }

    data class MessageTextBuilder(
        var message: String = ""
    )

    data class MessageImageBuilder(
        var imageUrl: String = "",
        var thumbUrl: String = ""
    )

    companion object {

        fun build(
            messageInfo: MessageInfo,
            textBuilder: MessageTextBuilder.() -> Unit
        ): TextMessages {
            val messageTextBuilder = MessageTextBuilder().apply(textBuilder)
            val currentChatingan = Chatingan.getInstance()
            val currentConfig = currentChatingan.getConfiguration()

            return TextMessages(
                id = UUID.randomUUID().toString(),
                messageInfoId = messageInfo.id,
                senderId = currentConfig.contact.id,
                receiverId = messageInfo.receiver.id,
                status = Status.SENDING,
                messageBody = messageTextBuilder.message,
                date = Utils.now().toDate()
            )
        }

        /*fun build(
            messageInfo: MessageInfo,
            textBuilder: MessageTextBuilder.() -> Unit
        ): ImageMessages {
            val messageTextBuilder = MessageTextBuilder().apply(textBuilder)
            val currentChatingan = Chatingan.getInstance()
            val currentConfig = currentChatingan.getConfiguration()

            return TextMessages(
                id = UUID.randomUUID().toString(),
                messageInfoId = messageInfo.id,
                senderId = currentConfig.contact.id,
                receiverId = messageInfo.receiver.id,
                status = Status.SENDING,
                messageBody = messageTextBuilder.message,
                lastUpdate = Utils.now().toDate()
            )
        }*/
    }
}