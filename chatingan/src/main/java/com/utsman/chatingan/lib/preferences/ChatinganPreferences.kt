package com.utsman.chatingan.lib.preferences

import android.content.Context
import android.content.SharedPreferences
import com.utsman.chatingan.lib.utils.Utils
import com.utsman.chatingan.lib.utils.toJson

internal object ChatinganPreferences {

    private const val CHATINGAN_PREF = "ChatinganPreferences"

    private fun sharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(CHATINGAN_PREF, Context.MODE_PRIVATE)
    }

    fun saveString(context: Context, key: String, value: String) {
        sharedPref(context).edit().putString(key, value).apply()
    }

    fun readString(context: Context, key: String): String {
        return sharedPref(context).getString(key, "").orEmpty()
    }

    inline fun <reified T>save(context: Context, key: String, data: T) {
        val dataString = data.toJson()
        saveString(context, key, dataString)
    }

    inline fun <reified T>read(context: Context, key: String): T? {
        val dataString = readString(context, key)
        return if (dataString.isEmpty()) {
            null
        } else {
            Utils.convertFromJson(dataString)
        }
    }
}