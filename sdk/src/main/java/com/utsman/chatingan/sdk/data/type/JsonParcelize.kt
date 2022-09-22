package com.utsman.chatingan.sdk.data.type

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

interface JsonParcelize {
    fun toJson(): String
    fun toJsonUri(): String

    companion object {
        fun <T>toObject(json: String?): T {
            val type = object : TypeToken<T>() {}.type
            return Gson().fromJson(json, type)
        }

        inline fun <reified T>toObjectUri(json: String?): T {
            val decodeJson = Uri.decode(json)
            val type = object : TypeToken<T>() {}.type
            return Gson().fromJson(decodeJson, type)
        }
    }
}