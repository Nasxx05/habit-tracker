package com.habitstreak.app.model

import java.time.LocalDate
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val createdDate: LocalDate = LocalDate.now(),
    val completionDates: List<LocalDate> = emptyList()
) {
    val isCompletedToday: Boolean
        get() = completionDates.contains(LocalDate.now())

    val currentStreak: Int
        get() {
            if (completionDates.isEmpty()) return 0

            val sorted = completionDates.distinct().sortedDescending()
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            // Streak must include today or yesterday
            val mostRecent = sorted.first()
            if (mostRecent != today && mostRecent != yesterday) return 0

            var streak = 1
            for (i in 1 until sorted.size) {
                val expected = sorted[0].minusDays(i.toLong())
                if (sorted[i] == expected) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }

    val longestStreak: Int
        get() {
            if (completionDates.isEmpty()) return 0

            val sorted = completionDates.distinct().sorted()
            var maxStreak = 1
            var currentRun = 1

            for (i in 1 until sorted.size) {
                if (sorted[i] == sorted[i - 1].plusDays(1)) {
                    currentRun++
                    if (currentRun > maxStreak) maxStreak = currentRun
                } else {
                    currentRun = 1
                }
            }
            return maxStreak
        }

    val streakFireEmojis: String
        get() = when (currentStreak) {
            0 -> ""
            in 1..6 -> "\uD83D\uDD25"
            in 7..13 -> "\uD83D\uDD25\uD83D\uDD25"
            in 14..29 -> "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"
            else -> "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"
        }

    fun toggleToday(): Habit {
        val today = LocalDate.now()
        return if (completionDates.contains(today)) {
            copy(completionDates = completionDates - today)
        } else {
            copy(completionDates = completionDates + today)
        }
    }

    fun completedOn(date: LocalDate): Boolean = completionDates.contains(date)
}
