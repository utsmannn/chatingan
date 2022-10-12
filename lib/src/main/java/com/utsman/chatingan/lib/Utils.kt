package com.utsman.chatingan.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.lib.data.model.Message

object Utils {
    fun now(): Long = System.currentTimeMillis()

    inline fun <reified T>convertFromJson(json: String): T {
        val typeToken = object : TypeToken<T>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    // val typeAdapter: RuntimeTypeAdapterFactory<Message> = RuntimeTypeAdapterFactory.of(Message::class.java)
    //                    .registerSubtype(Message.TextMessages::class.java)
    //                    .registerSubtype(Message.ImageMessages::class.java)

}