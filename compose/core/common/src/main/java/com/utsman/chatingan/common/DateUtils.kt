package com.utsman.chatingan.common

import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun toReadable(date: Date, pattern: String = "HH:mm"): String {
        val sdfHour = SimpleDateFormat(pattern)
        return sdfHour.format(date)
    }

    fun generateDateChat(date: Date): String {
        /*
        val currentDay = DividerCalculator.sdfDay.format(System.currentTimeMillis()).toInt()
        val messageToday = DividerCalculator.sdfDay.format(date.time).toInt()

        val format = when (currentDay) {
            messageToday -> {
                toReadable(date)
            }
            else -> {
                prettyTime.format(date)
            }
        }

        return format*/
        val prettyTime = PrettyTime()
        return prettyTime.format(date)
    }
}