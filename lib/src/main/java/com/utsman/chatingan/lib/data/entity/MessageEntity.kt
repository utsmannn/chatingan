package com.utsman.chatingan.lib.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val messageInfoId: String,
    val type: String,
    val senderId: String,
    val receiverId: String,
    val status: String,
    val messageBody: String,
    val date: Long
)