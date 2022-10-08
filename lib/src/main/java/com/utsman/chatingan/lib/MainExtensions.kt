package com.utsman.chatingan.lib

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.entity.MessageInfoEntity
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import java.lang.reflect.Type
import java.util.*

fun Long.toDate(): Date = Date(this)
fun Date.toLong(): Long = this.time

inline fun <reified T>T.typeToken(): Type {
    return object : TypeToken<T>() {}.type
}

inline fun <reified T>T.toJson(): String {
    val type = typeToken()
    return Gson().toJson(this, type)
}

/**
 * Sources java from:
 * https://stackoverflow.com/a/3657496/8581826
 * */
fun String.ellipsize(max: Int): String {
    val nonThin = "[^iIl1\\.,']"

    val textWidth: (str: String) -> Int = {
        (it.length - it.replace(nonThin.toRegex(), "").length / 2)
    }

    if (textWidth(this) <= max) return this

    var end = this.lastIndexOf(' ', max - 3)
    if (end == -1) return this.substring(0, max - 3) + "..."
    var newEnd = end
    do {
        end = newEnd
        newEnd = this.indexOf(' ', end + 1)
        if (newEnd == -1) newEnd = this.length
    } while (textWidth(this.substring(0, newEnd) + "...") < max)
    return this.substring(0, end) + "..."
}

fun MessageEntity.changeStatus(status: Message.Status): MessageEntity {
    return MessageEntity(
        id = id,
        messageInfoId = messageInfoId,
        type = type,
        senderId = senderId,
        receiverId = receiverId,
        status = status.name,
        messageBody = messageBody,
        date = date
    )
}

fun MessageInfoEntity.updateTime(): MessageInfoEntity {
    return MessageInfoEntity(
        id = id,
        receiverId = receiverId,
        lastUpdate = Utils.now()
    )
}

fun Contact.isValid(): Boolean {
    return name != "unknown"
}