package com.utsman.chatingan.sdk.data.entity

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utsman.chatingan.sdk.data.contract.Entity
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

data class Contact(
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var detail: Detail = Detail(),
    var lastUpdate: Date = Date.from(Instant.now())
) : Entity {
    override fun toJson(): String {
        val type = object : TypeToken<Contact>() {}.type
        return Gson().toJson(this, type)
    }

    override fun toJsonUri(): String {
        return Uri.encode(toJson())
    }

    data class Detail(
        var email: String = ""
    )

    companion object {
        fun getType(): Type {
            return object : TypeToken<Contact>() {}.type
        }
    }
}