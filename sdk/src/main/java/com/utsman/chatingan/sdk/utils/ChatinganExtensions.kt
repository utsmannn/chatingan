package com.utsman.chatingan.sdk.utils

import com.utsman.chatingan.sdk.data.config.ChatinganConfig
import com.utsman.chatingan.sdk.data.entity.MessageChat

fun MessageChat.isFromMe(config: ChatinganConfig): Boolean {
    return senderId == config.contact.id
}

fun MessageChat.isAllRead(): Boolean {
    return readByIds.contains(senderId) and readByIds.contains(receiverId)
}