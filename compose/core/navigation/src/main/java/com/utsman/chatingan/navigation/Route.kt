package com.utsman.chatingan.navigation

import android.net.Uri
import com.google.gson.Gson

open class Route(
    private val value: String = ""
) {
    protected open val arg: String = ""

    fun getValue(): String {
        return if (arg.isEmpty()) {
            value
        } else {
            "$value/{${arg}}"
        }
    }

    private fun generateValueWithArgumentContent(content: String): Route {
        val newValue = if (arg.isEmpty()) {
            value
        } else {
            "$value/$content"
        }

        return Route(newValue)
    }

    fun <T>getValueWithArgumentContent(content: T, clazz: Class<T>): Route {
        val json = Gson().toJson(content, clazz)
        val jsonUri = Uri.encode(json)
        return generateValueWithArgumentContent(jsonUri)
    }

    companion object {
        val empty: Route
            get() = Route("")
    }
}