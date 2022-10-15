package com.utsman.chatingan.lib.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: String,
    val fcmToken: String,
    val isTyping: Boolean,
    val lastMessageId: String,
    val lastMessageUpdate: Long,
    val lastUpdate: Long
)