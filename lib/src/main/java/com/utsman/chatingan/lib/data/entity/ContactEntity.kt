package com.utsman.chatingan.lib.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class ContactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val imageUrl: String,
    val fcmToken: String,
    val lastUpdate: Long
)