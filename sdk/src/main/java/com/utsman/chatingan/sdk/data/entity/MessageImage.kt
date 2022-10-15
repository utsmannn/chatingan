package com.utsman.chatingan.sdk.data.entity

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class MessageImage(
    var encodedBlur: String = "",
    var name: String = "",
    var caption: String = ""
) {

    fun toJson(): String {
        val type = object : TypeToken<MessageImage>() {}.type
        return Gson().toJson(this, type)
    }
}