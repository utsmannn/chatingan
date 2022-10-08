package com.utsman.chatingan.lib.data.transaction

import androidx.room.Embedded
import androidx.room.Relation
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageInfoEntity

data class MessagesInfoAndReceiverContact(
    @Embedded val receiverEntity: ContactEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiverId"
    )
    val messageInfoEntities: List<MessageInfoEntity>
)