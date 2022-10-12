package com.utsman.chatingan.lib

import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.transaction.ContactAndLastMessage

object DataMapper {

    fun mapContactToEntity(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            name = contact.name,
            email = contact.email,
            imageUrl = contact.imageUrl,
            fcmToken = contact.fcmToken,
            lastMessageId = contact.lastMessageId,
            isTyping = false,
            lastMessageUpdate = contact.lastMessageUpdate.toLong(),
            lastUpdate = contact.lastUpdate.toLong()
        )
    }

    fun mapEntityToContact(entity: ContactEntity): Contact {
        return Contact(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            imageUrl = entity.imageUrl,
            fcmToken = entity.fcmToken,
            lastMessageId = entity.lastMessageId,
            isTyping = entity.isTyping,
            lastMessageUpdate = entity.lastMessageUpdate.toDate(),
            lastUpdate = entity.lastUpdate.toDate()
        )
    }

    fun mapMessageToEntity(message: Message): MessageEntity {
        return when (message) {
            is Message.TextMessages -> MessageEntity(
                id = message.id,
                type = Message.Type.TEXT.name,
                senderId = message.senderId,
                receiverId = message.receiverId,
                status = message.status.name,
                messageBody = message.messageBody,
                date = message.date.toLong()
            )
            else -> throw ChatinganException(ErrorMessages.MESSAGE_INVALID_TYPE)
        }
    }

    fun mapEntityToMessage(entity: MessageEntity): Message {
        return when (entity.type.uppercase().run { Message.Type.valueOf(this) }) {
            Message.Type.TEXT -> {
                Message.TextMessages(
                    id = entity.id,
                    senderId = entity.senderId,
                    receiverId = entity.receiverId,
                    status = entity.status.uppercase().run { Message.Status.valueOf(this) },
                    messageBody = entity.messageBody,
                    date = entity.date.toDate()
                )
            }
            else -> throw ChatinganException(ErrorMessages.MESSAGE_INVALID_TYPE)
        }
    }

    fun mapContactAndLastMessageToMessageInfo(
        contactAndLastMessage: ContactAndLastMessage,
        unreadCount: Int
    ): MessageInfo {
        val receiverEntity = contactAndLastMessage.contactEntity
        val receiver = mapEntityToContact(receiverEntity)
        val lastMessage = contactAndLastMessage.lastMessageEntity
            .lastOrNull()
            ?.run {
                mapEntityToMessage(this)
            } ?: Message.emptyTextMessage()
        val lastMessageUpdate = when (lastMessage) {
            is Message.TextMessages -> lastMessage.date
            is Message.ImageMessages -> lastMessage.date
            is Message.DividerMessage -> lastMessage.date
        }

        return MessageInfo(
            id = receiver.id,
            receiver = receiver,
            lastMessage = lastMessage,
            lastUpdate = lastMessageUpdate,
            unreadCount = unreadCount,
            isTyping = receiverEntity.isTyping
        )
    }

    object ErrorMessages {
        internal const val MESSAGE_INVALID_TYPE = "Invalid messages type!"
    }
}