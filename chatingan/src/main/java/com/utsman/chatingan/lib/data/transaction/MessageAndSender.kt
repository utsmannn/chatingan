package com.utsman.chatingan.lib.data.transaction

import androidx.room.Embedded
import androidx.room.Relation
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity

data class MessageAndSender(
    @Embedded val messages: MessageEntity,
    @Relation(
        parentColumn = "senderId",
        entityColumn = "id"
    )
    val contactEntity: List<ContactEntity>
)