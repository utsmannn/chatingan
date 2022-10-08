package com.utsman.chatingan.lib

import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.entity.MessageInfoEntity
import com.utsman.chatingan.lib.data.transaction.MessagesInfoAndReceiverContact
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import com.utsman.chatingan.lib.data.pair.ContactPair
import java.util.UUID

object DataMapper {

    fun mapContactToEntity(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            name = contact.name,
            email = contact.email,
            imageUrl = contact.imageUrl,
            fcmToken = contact.fcmToken,
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
            lastUpdate = entity.lastUpdate.toDate()
        )
    }

    fun mapMessageInfoToEntity(messageInfo: MessageInfo): MessageInfoEntity {
        return MessageInfoEntity(
            id = messageInfo.id,
            receiverId = messageInfo.receiver.id,
            lastUpdate = messageInfo.lastUpdate.toLong()
        )
    }

    fun mapMessageToEntity(message: Message): MessageEntity {
        return when (message) {
            is Message.TextMessages -> MessageEntity(
                id = message.id,
                messageInfoId = message.messageInfoId,
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
                    messageInfoId = entity.messageInfoId,
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

    fun mapMessagesInfoAndReceiverContactToMessagesInfo(
        messagesInfoAndReceiverContact: MessagesInfoAndReceiverContact
    ): MessageInfo {
        val messageInfo = messagesInfoAndReceiverContact.messageInfoEntities.firstOrNull()
            ?: throw ChatinganException("Messages Info is null!")
        val receiver = messagesInfoAndReceiverContact.receiverEntity
        return MessageInfo(
            id = messageInfo.id,
            receiver = receiver.run { mapEntityToContact(this) },
            lastUpdate = messageInfo.lastUpdate.toDate()
        )
    }

    fun mapContactToPairData(contact: Contact): ContactPair {
        return ContactPair(
            name = contact.name,
            email = contact.email,
            imageUrl = contact.imageUrl,
            fcmToken = contact.fcmToken
        )
    }

    fun mapContactPairToContact(contactPair: ContactPair): Contact {
        return Contact(
            id = UUID.randomUUID().toString(),
            name = contactPair.name,
            email = contactPair.email,
            imageUrl = contactPair.imageUrl,
            fcmToken = contactPair.fcmToken,
            lastUpdate = Utils.now().toDate()
        )
    }

    object ErrorMessages {
        internal const val MESSAGE_INVALID_TYPE = "Invalid messages type!"
    }
}