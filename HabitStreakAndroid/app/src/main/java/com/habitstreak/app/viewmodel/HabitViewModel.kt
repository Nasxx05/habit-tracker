package com.habitstreak.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.habitstreak.app.model.Habit
import java.time.LocalDate
import java.time.YearMonth

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        out.value(value?.toString())
    }

    override fun read(reader: JsonReader): LocalDate {
        return LocalDate.parse(reader.nextString())
    }
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> get() = _habits

    val showMilestoneConfetti = mutableStateOf(false)
    val milestoneMessage = mutableStateOf("")

    val isFirstLaunch: Boolean
        get() = !prefs.getBoolean("has_launched_before", false)

    val completedTodayCount: Int
        get() = _habits.count { it.isCompletedToday }

    val remainingTodayCount: Int
        get() = _habits.size - completedTodayCount

    val allCompletedToday: Boolean
        get() = _habits.isNotEmpty() && _habits.all { it.isCompletedToday }

    init {
        loadHabits()
    }

    // MARK: - Persistence

    private fun loadHabits() {
        val json = prefs.getString("saved_habits", null) ?: return
        val type = object : TypeToken<List<Habit>>() {}.type
        val loaded: List<Habit> = gson.fromJson(json, type)
        _habits.clear()
        _habits.addAll(loaded)
    }

    private fun saveHabits() {
        val json = gson.toJson(_habits.toList())
        prefs.edit().putString("saved_habits", json).apply()
    }

    fun markFirstLaunchDone() {
        prefs.edit().putBoolean("has_launched_before", true).apply()
    }

    // MARK: - CRUD

    fun addHabit(name: String, emoji: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val habit = Habit(name = trimmed, emoji = emoji.ifEmpty { "\u2B50" })
        _habits.add(habit)
        saveHabits()
    }

    fun deleteHabit(id: String) {
        _habits.removeAll { it.id == id }
        saveHabits()
    }

    fun updateHabit(id: String, name: String, emoji: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index < 0) return
        _habits[index] = _habits[index].copy(name = name.trim(), emoji = emoji)
        saveHabits()
    }

    // MARK: - Toggle

    fun toggleHabit(id: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index < 0) return

        val wasCompleted = _habits[index].isCompletedToday
        _habits[index] = _habits[index].toggleToday()
        saveHabits()

        if (!wasCompleted) {
            val streak = _habits[index].currentStreak
            if (streak in listOf(7, 14, 30, 50, 100, 365)) {
                milestoneMessage.value = "${_habits[index].emoji} $streak day streak!"
                showMilestoneConfetti.value = true
            }
        }
    }

    // MARK: - Statistics

    fun completionRate(yearMonth: YearMonth): Double {
        val today = LocalDate.now()
        var totalPossible = 0
        var totalCompleted = 0

        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            if (date.isAfter(today)) break

            for (habit in _habits) {
                if (!date.isBefore(habit.createdDate)) {
                    totalPossible++
                    if (habit.completedOn(date)) totalCompleted++
                }
            }
        }

        return if (totalPossible > 0) totalCompleted.toDouble() / totalPossible else 0.0
    }

    fun perfectDays(yearMonth: YearMonth): Int {
        val today = LocalDate.now()
        var count = 0

        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            if (date.isAfter(today)) break

            val activeHabits = _habits.filter { !date.isBefore(it.createdDate) }
            if (activeHabits.isNotEmpty() && activeHabits.all { it.completedOn(date) }) {
                count++
            }
        }
        return count
    }

    fun bestStreak(): Int = _habits.maxOfOrNull { it.longestStreak } ?: 0

    fun dayStatus(date: LocalDate): DayCompletionStatus {
        val today = LocalDate.now()
        if (date.isAfter(today)) return DayCompletionStatus.FUTURE

        val activeHabits = _habits.filter { !date.isBefore(it.createdDate) }
        if (activeHabits.isEmpty()) return DayCompletionStatus.NO_HABITS

        val completedCount = activeHabits.count { it.completedOn(date) }
        return when (completedCount) {
            activeHabits.size -> DayCompletionStatus.ALL_COMPLETED
            0 -> DayCompletionStatus.NONE_COMPLETED
            else -> DayCompletionStatus.SOME_COMPLETED
        }
    }

    fun hasDuplicateName(name: String, excludeId: String? = null): Boolean {
        val trimmed = name.trim().lowercase()
        return _habits.any { it.id != excludeId && it.name.lowercase() == trimmed }
    }
}

enum class DayCompletionStatus {
    ALL_COMPLETED,
    SOME_COMPLETED,
    NONE_COMPLETED,
    FUTURE,
    NO_HABITS
}
