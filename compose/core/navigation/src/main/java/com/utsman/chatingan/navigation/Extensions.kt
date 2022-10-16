package com.utsman.chatingan.navigation

import androidx.navigation.NavBackStackEntry
import com.utsman.chatingan.lib.utils.Utils

inline fun <reified T> NavBackStackEntry.generateDataFromKey(key: String): T {
    val json = arguments?.getString(key)
    return Utils.convertFromJson(json.orEmpty())
}