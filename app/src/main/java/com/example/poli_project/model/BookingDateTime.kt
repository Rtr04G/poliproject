package com.example.poli_project.model

import org.json.JSONObject
import org.joda.time.DateTime

data class BookingDateTime(
    val date: String,
    val availableTimes: List<String> = listOf(),
    val bookedTimes: List<String> = listOf()
) {
    val isFullyBooked: Boolean
        get() = bookedTimes.size >= 17

    fun isTimeBooked(time: String): Boolean {
        return time in bookedTimes
    }
}

fun parseBookingDates(jsonResponse: String): List<BookingDateTime> {
    val bookingDates = mutableListOf<BookingDateTime>()
    val jsonObject = JSONObject(jsonResponse)
    val dataTimesArray = jsonObject.getJSONArray("datatimes")


    val allAvailableTimes = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "12:00", "13:30", "14:00", "14:30",
        "15:00", "15:30", "16:00", "16:30", "17:00"
    )

    var timesByDate = mutableMapOf<String, MutableList<String>>()
    for (i in 0 until dataTimesArray.length()) {
        val datetime = dataTimesArray.getJSONObject(i).getString("datetime$i")
        val (date, time) = datetime.split("T")
        timesByDate.getOrPut(date) { mutableListOf() }.add(time.substring(0, 5))
    }
    var newDate = DateTime.now().plusDays(1)
    while (newDate.minusDays(60).isBeforeNow){
        if (!timesByDate.containsKey(newDate.toLocalDate().toString())) {
            timesByDate.getOrPut(newDate.toLocalDate().toString()) { mutableListOf() }.add("")
        }
        newDate = newDate.plusDays(1)
    }
    timesByDate = timesByDate.toSortedMap()
    for ((date, bookedTimes) in timesByDate) {
        val availableTimes = allAvailableTimes - bookedTimes
        bookingDates.add(
            BookingDateTime(
                date = date,
                availableTimes = availableTimes,
                bookedTimes = bookedTimes
            )
        )
    }

    return bookingDates
}
