package com.utsman.chatingan.sdk.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun toReadable(date: Date): String {
        val sdfHour = SimpleDateFormat("HH:mm")
        return sdfHour.format(date)
    }
}