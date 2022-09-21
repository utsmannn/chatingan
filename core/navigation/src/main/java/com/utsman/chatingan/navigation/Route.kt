package com.utsman.chatingan.navigation

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

    fun getValueWithArgumentContent(content: String): Route {
        val newValue = if (arg.isEmpty()) {
            value
        } else {
            "$value/$content"
        }

        return Route(newValue)
    }

    companion object {
        val empty: Route
            get() = Route("")
    }
}