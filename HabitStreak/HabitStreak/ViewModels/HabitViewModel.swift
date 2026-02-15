import Foundation
import Combine

class HabitViewModel: ObservableObject {
    @Published var habits: [Habit] = []
    @Published var showMilestoneConfetti: Bool = false
    @Published var milestoneMessage: String = ""

    private let saveKey = "saved_habits"
    private let firstLaunchKey = "has_launched_before"
    private let lastOpenDateKey = "last_open_date"

    var isFirstLaunch: Bool {
        !UserDefaults.standard.bool(forKey: firstLaunchKey)
    }

    var completedTodayCount: Int {
        habits.filter(\.isCompletedToday).count
    }

    var remainingTodayCount: Int {
        habits.count - completedTodayCount
    }

    var allCompletedToday: Bool {
        !habits.isEmpty && habits.allSatisfy(\.isCompletedToday)
    }

    init() {
        loadHabits()
    }

    // MARK: - Persistence

    func loadHabits() {
        guard let data = UserDefaults.standard.data(forKey: saveKey),
              let decoded = try? JSONDecoder().decode([Habit].self, from: data) else {
            return
        }
        habits = decoded
    }

    func saveHabits() {
        guard let encoded = try? JSONEncoder().encode(habits) else { return }
        UserDefaults.standard.set(encoded, forKey: saveKey)
    }

    func markFirstLaunchDone() {
        UserDefaults.standard.set(true, forKey: firstLaunchKey)
    }

    func updateLastOpenDate() {
        UserDefaults.standard.set(Date(), forKey: lastOpenDateKey)
    }

    // MARK: - CRUD Operations

    func addHabit(name: String, emoji: String) {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedName.isEmpty else { return }

        let habit = Habit(name: trimmedName, emoji: emoji.isEmpty ? "\u{2B50}" : emoji)
        habits.append(habit)
        saveHabits()
    }

    func deleteHabit(at offsets: IndexSet) {
        habits.remove(atOffsets: offsets)
        saveHabits()
    }

    func deleteHabit(id: UUID) {
        habits.removeAll { $0.id == id }
        saveHabits()
    }

    func updateHabit(id: UUID, name: String, emoji: String) {
        guard let index = habits.firstIndex(where: { $0.id == id }) else { return }
        habits[index].name = name.trimmingCharacters(in: .whitespacesAndNewlines)
        habits[index].emoji = emoji
        saveHabits()
    }

    func moveHabit(from source: IndexSet, to destination: Int) {
        habits.move(fromOffsets: source, toOffset: destination)
        saveHabits()
    }

    // MARK: - Toggle Completion

    func toggleHabit(id: UUID) {
        guard let index = habits.firstIndex(where: { $0.id == id }) else { return }

        let wasCompleted = habits[index].isCompletedToday
        habits[index].toggleToday()
        saveHabits()

        // Check for milestone after completing (not uncompleting)
        if !wasCompleted {
            let streak = habits[index].currentStreak
            if [7, 14, 30, 50, 100, 365].contains(streak) {
                milestoneMessage = "\(habits[index].emoji) \(streak) day streak!"
                showMilestoneConfetti = true
            }
        }
    }

    // MARK: - Statistics

    func completionRate(for month: Date) -> Double {
        let calendar = Calendar.current
        let now = Date()
        let daysInMonth = DateHelper.daysInMonth(for: month)

        var totalPossible = 0
        var totalCompleted = 0

        for day in 1...daysInMonth {
            let date = DateHelper.dateFor(day: day, inMonthOf: month)
            // Skip future days
            if date > now { break }

            for habit in habits {
                // Only count days after habit was created
                if calendar.startOfDay(for: date) >= calendar.startOfDay(for: habit.createdDate) {
                    totalPossible += 1
                    if habit.completedOn(date: date) {
                        totalCompleted += 1
                    }
                }
            }
        }

        guard totalPossible > 0 else { return 0 }
        return Double(totalCompleted) / Double(totalPossible)
    }

    func perfectDays(for month: Date) -> Int {
        let calendar = Calendar.current
        let now = Date()
        let daysInMonth = DateHelper.daysInMonth(for: month)
        var count = 0

        for day in 1...daysInMonth {
            let date = DateHelper.dateFor(day: day, inMonthOf: month)
            if date > now { break }

            let activeHabits = habits.filter {
                calendar.startOfDay(for: date) >= calendar.startOfDay(for: $0.createdDate)
            }

            guard !activeHabits.isEmpty else { continue }

            if activeHabits.allSatisfy({ $0.completedOn(date: date) }) {
                count += 1
            }
        }

        return count
    }

    func bestStreakThisMonth(for month: Date) -> Int {
        habits.map(\.longestStreak).max() ?? 0
    }

    func dayStatus(date: Date) -> DayCompletionStatus {
        let calendar = Calendar.current
        let now = Date()

        if calendar.startOfDay(for: date) > calendar.startOfDay(for: now) {
            return .future
        }

        let activeHabits = habits.filter {
            calendar.startOfDay(for: date) >= calendar.startOfDay(for: $0.createdDate)
        }

        guard !activeHabits.isEmpty else { return .noHabits }

        let completedCount = activeHabits.filter { $0.completedOn(date: date) }.count

        if completedCount == activeHabits.count {
            return .allCompleted
        } else if completedCount > 0 {
            return .someCompleted
        } else {
            return .noneCompleted
        }
    }

    func hasDuplicateName(_ name: String, excludingId: UUID? = nil) -> Bool {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        return habits.contains { habit in
            habit.id != excludingId && habit.name.lowercased() == trimmed
        }
    }
}

enum DayCompletionStatus {
    case allCompleted
    case someCompleted
    case noneCompleted
    case future
    case noHabits
}
