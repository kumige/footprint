package com.example.sensorapp

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class Utils {

    private val gson = Gson()

    // Format timer to xx:xx:xx
    fun formatTimer(time: Int, originCode: Int): String {
        var sec = time
        var min = 0
        var h = 0

        if (sec / 60 > 0) {
            min = sec / 60
            sec -= 60 * min
        }

        if (min / 60 > 0) {
            h = min / 60
            min -= 60 * h
        }

        val hours = if (h < 10) {
            "0$h"
        } else "$h"
        val minutes = if (min < 10) {
            "0$min"
        } else "$min"
        val seconds = if (sec < 10) {
            "0$sec"
        } else "$sec"


        return when (originCode) {
            0 -> "$hours:$minutes:$seconds"
            1 -> when {
                min == 0 -> seconds
                h == 0 -> "$minutes:$seconds"
                else -> "$hours:$minutes:$seconds"
            }
            else -> "$hours:$minutes:$seconds"
        }

    }

    @TypeConverter
    fun historyToJsonString(item: History): String {
        return gson.toJson(item)
    }

    @TypeConverter
    fun stringToHistoryObject(data: String?): History {
        if (data == null) {
            return History(0,"0","0",0,0,"")
        }

        val objectType = object : TypeToken<History>() {

        }.type

        return gson.fromJson(data, objectType)
    }

}