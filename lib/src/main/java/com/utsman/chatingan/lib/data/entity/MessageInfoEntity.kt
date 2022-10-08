package com.utsman.chatingan.lib.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.utsman.chatingan.lib.data.model.Contact
import java.util.UUID

@Entity
data class MessageInfoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val receiverId: String,
    val lastUpdate: Long
)