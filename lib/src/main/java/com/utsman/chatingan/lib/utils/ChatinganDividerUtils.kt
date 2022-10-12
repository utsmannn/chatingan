package com.utsman.chatingan.lib.utils

import com.utsman.chatingan.lib.data.model.Message
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

object ChatinganDividerUtils {
    private val sdfDay = SimpleDateFormat("DD")

    fun calculateDividerChat(oldList: List<Message>): MutableList<Message> {
        val prettyTime = PrettyTime()

        val newList: MutableList<Message> = mutableListOf()
        val iterator = oldList.iterator()
        var currentDay = 0
        for (item in iterator) {
            val day = sdfDay.format(item.superDate).toInt()
            if (day > currentDay) {
                currentDay = day
                val divider = Message.DividerMessage(item.superDate)
                newList.add(divider)
            }

            newList.add(item)
        }
        return newList
    }

    fun generateDateDividerText(date: Date): String {
        val prettyTime = PrettyTime()
        val currentDay = sdfDay.format(System.currentTimeMillis()).toInt()
        val messageToday = sdfDay.format(date.time).toInt()

        val format = when (currentDay) {
            messageToday -> {
                "Today"
            }
            else -> {
                prettyTime.format(date)
            }
        }

        return format
    }
}