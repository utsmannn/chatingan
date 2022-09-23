package com.utsman.chatingan.sdk.data.entity

import java.util.*

data class Chat(
    val id: String = UUID.randomUUID().toString(),
    var contacts: List<Contact> = emptyList(),
    var messages: List<MessageChat> = emptyList(),
    var chatInfo: ChatInfo = ChatInfo()
) {
}