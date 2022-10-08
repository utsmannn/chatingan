package com.utsman.chatingan.lib

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal object Utils {
    fun now(): Long = System.currentTimeMillis()

    inline fun <reified T>convertFromJson(json: String): T {
        val typeToken = object : TypeToken<T>() {}.type
        return Gson().fromJson(json, typeToken)
    }
}