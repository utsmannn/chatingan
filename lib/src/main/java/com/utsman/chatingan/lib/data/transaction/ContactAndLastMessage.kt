package com.utsman.chatingan.lib.data.transaction

import androidx.room.Embedded
import androidx.room.Relation
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity

data class ContactAndLastMessage(
    @Embedded val contactEntity: ContactEntity,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val lastMessageEntity: List<MessageEntity>
)