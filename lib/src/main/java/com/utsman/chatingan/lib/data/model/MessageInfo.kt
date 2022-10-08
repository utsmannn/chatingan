package com.utsman.chatingan.lib.data.model

import com.utsman.chatingan.lib.Utils
import com.utsman.chatingan.lib.toDate
import java.util.*

data class MessageInfo(
    val id: String = "",
    val receiver: Contact,
    val lastUpdate: Date = Utils.now().toDate()
) {

    companion object {
        fun empty(contact: Contact): MessageInfo {
            return MessageInfo(
                id = "",
                receiver = contact,
                lastUpdate = Utils.now().toDate()
            )
        }
    }
}