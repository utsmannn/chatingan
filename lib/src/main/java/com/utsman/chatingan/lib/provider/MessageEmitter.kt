package com.utsman.chatingan.lib.provider

import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.network.NotifierResult
import com.utsman.chatingan.lib.receiver.MessageNotifier

abstract class MessageEmitter {
    abstract suspend fun sendNotifier(
        contact: Contact,
        json: String,
        notificationType: MessageNotifier.NotificationType,
        messageType: Message.Type,
        title: String = "",
        subtitle: String = ""
    ): NotifierResult
}