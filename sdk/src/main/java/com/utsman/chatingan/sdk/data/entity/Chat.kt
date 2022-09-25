package com.utsman.chatingan.sdk.data.entity

import java.util.*

data class Chat(
    val id: String = UUID.randomUUID().toString(),
    var contact: Contact = Contact(),
    var messages: List<MessageChat> = emptyList(),
    var chatInfo: ChatInfo = ChatInfo()
)