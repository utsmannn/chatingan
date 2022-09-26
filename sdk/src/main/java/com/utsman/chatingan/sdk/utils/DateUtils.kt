package com.utsman.chatingan.sdk.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun toReadable(date: Date, pattern: String = "HH:mm"): String {
        val sdfHour = SimpleDateFormat(pattern)
        return sdfHour.format(date)
    }

    fun generateDateChat(date: Date): String {
        val currentDay = DividerCalculator.sdfDay.format(System.currentTimeMillis()).toInt()
        val messageToday = DividerCalculator.sdfDay.format(date.time).toInt()

        val format = when (currentDay) {
            messageToday -> {
                toReadable(date)
            }
            messageToday - 1 -> {
                "Kemarin"
            }
            else -> {
                toReadable(date, "dd/MMM/yyyy")
            }
        }

        return format
    }
}