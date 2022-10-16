package com.utsman.chatingan.lib.data.model

import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.utils.toDate
import java.util.*

data class MessageInfo(
    val id: String = "",
    val receiver: Contact,
    val lastMessage: Message,
    val isTyping: Boolean,
    val unreadCount: Int,
    val lastUpdate: Date = Utils.now().toDate()
)