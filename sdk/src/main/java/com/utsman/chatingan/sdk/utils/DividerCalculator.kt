package com.utsman.chatingan.sdk.utils

import android.annotation.SuppressLint
import com.utsman.chatingan.sdk.data.entity.MessageChat
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.Instant

@SuppressLint("SimpleDateFormat")
object DividerCalculator {

    internal val sdfDay = SimpleDateFormat("DD")

    fun calculateDividerChat(oldList: List<MessageChat>): MutableList<MessageChat> {
        val prettyTime = PrettyTime()

        val newList: MutableList<MessageChat> = mutableListOf()
        val iterator = oldList.iterator()
        var currentDay = 0
        for (item in iterator) {
            val day = sdfDay.format(item.lastUpdate).toInt()
            if (day > currentDay) {
                val newIdDivider = "divider-$day"
                currentDay = day
                val messageDivider = prettyTime.format(item.lastUpdate)

                val chatDivider = MessageChat(
                    id = newIdDivider,
                    messageBody = messageDivider,
                    type = MessageChat.Type.DIVIDER,
                    lastUpdate = item.lastUpdate
                )

                newList.add(chatDivider)
            }

            newList.add(item)
        }
        return newList
    }

    /*fun calculateDividerGallery(oldList: List<PhotoLocal>): MutableList<PhotoLocal> {
        val newList: MutableList<PhotoLocal> = mutableListOf()
        var currentDay = 0
        for (item in oldList.iterator()) {
            logi(item.toString())
            val newTime = item.date.toLong()*1000
            val day = sdfDay.format(newTime).toInt()
            if (day > currentDay) {
                currentDay = day
                val messageDivider = when (currentDay) {
                    nowDay -> {
                        "Hari ini"
                    }
                    nowDay - 1 -> {
                        "Kemarin"
                    }
                    else -> {
                        sdfDate.format(newTime)
                    }
                }

                logi("divider here --> $messageDivider")
                val photoDivider = photo {
                    dividerString = messageDivider
                }.apply {
                    photoType = PhotoType.DIVIDER
                }

                newList.add(photoDivider)
            }

            newList.add(item.apply { photoType = PhotoType.ITEM })
        }
        return newList
    }*/
}