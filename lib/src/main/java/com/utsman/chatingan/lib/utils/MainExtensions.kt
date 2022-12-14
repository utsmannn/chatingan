package com.utsman.chatingan.lib.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.lib.data.ChatinganException
import com.utsman.chatingan.lib.data.entity.ContactEntity
import com.utsman.chatingan.lib.data.entity.MessageEntity
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.preferences.ChatinganPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class CoroutineScopeIO : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO
}

@Suppress("FunctionName")
internal fun IOScope(): CoroutineScope = CoroutineScopeIO()

// Date.from(Instant.now())
internal fun now(): Date {
    return Calendar.getInstance().time
}

fun Long.toDate(): Date {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss")
    val dateString = sdf.format(this)
    return sdf.parse(dateString)
}

fun Date.toLong(): Long {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss")
    val dateString = sdf.format(this)
    return sdf.parse(dateString).time
}

inline fun <reified T> T.typeToken(): Type {
    return object : TypeToken<T>() {}.type
}

fun gsonDate(): Gson {
    return GsonBuilder()
        .setDateFormat("MMM dd, yyyy HH:mm:ss")
        .create()
}

internal inline fun <reified T> T.toJson(): String {
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

internal fun MessageEntity.changeStatus(status: Message.Status): MessageEntity {
    return MessageEntity(
        id = id,
        type = type,
        senderId = senderId,
        receiverId = receiverId,
        status = status.name,
        messageBody = messageBody,
        date = date
    )
}

internal fun ContactEntity.setTyping(isTyping: Boolean): ContactEntity {
    return ContactEntity(
        id = id,
        name = name,
        email = email,
        imageUrl = imageUrl,
        token = token,
        isTyping = isTyping,
        lastMessageId = lastMessageId,
        lastMessageUpdate = lastMessageUpdate,
        lastUpdate = lastUpdate
    )
}

fun Contact.isValid(): Boolean {
    return name != "unknown"
}

fun Message.ifTextMessage(message: (Message.TextMessages) -> Unit) {
    if (this is Message.TextMessages) {
        message.invoke(this)
    }
}

fun Message.ifImageMessage(message: (Message.ImageMessages) -> Unit) {
    if (this is Message.ImageMessages) {
        message.invoke(this)
    }
}

fun String.calculateLongId(): Long {
    val regexString = "[^A-Za-z]+".toRegex()
    val regexNumber = """[^0-9]""".toRegex()
    val outputChar = regexString.replace(this, "")
    val outputNumber = regexNumber.replace(this, "")

    val charArray = outputChar.lowercase().toCharArray()
    val resultString = charArray.map {
        val temp = it.code
        val code = 96
        if (temp in 97..122) {
            temp - code
        } else {
            null
        }
    }.filterNotNull().sum().toString()

    val finalNum = outputNumber.toLongOrNull() ?: 0
    val finalString = resultString.toLongOrNull() ?: 0
    return finalNum + finalString
}

fun String.calculateIntId(): Int {
    val regexString = "[^A-Za-z]+".toRegex()
    val regexNumber = """[^0-9]""".toRegex()
    val outputChar = regexString.replace(this, "")
    val outputNumber = regexNumber.replace(this, "")

    val charArray = outputChar.lowercase().toCharArray()
    val resultString = charArray.map {
        val temp = it.code
        val code = 96
        if (temp in 97..122) {
            temp - code
        } else {
            null
        }
    }.filterNotNull().sum().toString()

    val finalNum = outputNumber.toIntOrNull() ?: 0
    val finalString = resultString.toIntOrNull() ?: 0
    return finalNum + finalString
}

internal fun Context.getContact(): Contact {
    return ChatinganPreferences.read<Contact>(this, "contact")
        ?: throw ChatinganException("Yout contact not saved")
}