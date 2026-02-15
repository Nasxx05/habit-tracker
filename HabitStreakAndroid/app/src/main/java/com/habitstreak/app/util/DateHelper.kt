package com.habitstreak.app.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class TimeOfDay(val greeting: String) {
    MORNING("Good morning! \u2600\uFE0F"),
    AFTERNOON("Good afternoon! \uD83D\uDC4B"),
    EVENING("Good evening! \uD83C\uDF19"),
    NIGHT("Still up? \uD83E\uDD89")
}

object DateHelper {

    fun timeOfDay(time: LocalTime = LocalTime.now()): TimeOfDay = when (time.hour) {
        in 6..11 -> TimeOfDay.MORNING
        in 12..17 -> TimeOfDay.AFTERNOON
        in 18..23 -> TimeOfDay.EVENING
        else -> TimeOfDay.NIGHT
    }

    fun formattedDate(date: LocalDate = LocalDate.now()): String {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return "$dayOfWeek, ${date.format(formatter)}"
    }

    fun monthYearString(yearMonth: YearMonth): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return yearMonth.atDay(1).format(formatter)
    }

    private val motivationalMessages = listOf(
        "Keep the streak alive! \uD83D\uDD25",
        "You've got this! \uD83D\uDCAA",
        "One day at a time! \u2B50",
        "Consistency is key! \uD83C\uDFAF",
        "Build those habits! \uD83C\uDF31",
        "Stay focused! \uD83D\uDCA1",
        "Progress, not perfection! \uD83D\uDE80"
    )

    fun dailyMotivationalMessage(): String {
        val dayOfYear = LocalDate.now().dayOfYear
        return motivationalMessages[dayOfYear % motivationalMessages.size]
    }
}
