import Foundation

struct Habit: Identifiable, Codable, Equatable {
    let id: UUID
    var name: String
    var emoji: String
    let createdDate: Date
    var completionDates: [Date]

    init(id: UUID = UUID(), name: String, emoji: String, createdDate: Date = Date(), completionDates: [Date] = []) {
        self.id = id
        self.name = name
        self.emoji = emoji
        self.createdDate = createdDate
        self.completionDates = completionDates
    }

    // MARK: - Computed Properties

    var isCompletedToday: Bool {
        completionDates.contains { Calendar.current.isDateInToday($0) }
    }

    var currentStreak: Int {
        guard !completionDates.isEmpty else { return 0 }

        let calendar = Calendar.current
        let sortedDates = completionDates
            .map { calendar.startOfDay(for: $0) }
            .sorted(by: >)

        // Remove duplicates (same day)
        var uniqueDays: [Date] = []
        for date in sortedDates {
            if uniqueDays.last.map({ !calendar.isDate($0, inSameDayAs: date) }) ?? true {
                uniqueDays.append(date)
            }
        }

        let today = calendar.startOfDay(for: Date())
        let yesterday = calendar.date(byAdding: .day, value: -1, to: today)!

        // Streak must include today or yesterday to be "current"
        guard let mostRecent = uniqueDays.first,
              calendar.isDate(mostRecent, inSameDayAs: today) || calendar.isDate(mostRecent, inSameDayAs: yesterday) else {
            return 0
        }

        var streak = 1
        for i in 1..<uniqueDays.count {
            let expected = calendar.date(byAdding: .day, value: -i, to: uniqueDays[0])!
            if calendar.isDate(uniqueDays[i], inSameDayAs: expected) {
                streak += 1
            } else {
                break
            }
        }

        return streak
    }

    var longestStreak: Int {
        guard !completionDates.isEmpty else { return 0 }

        let calendar = Calendar.current
        let sortedDates = completionDates
            .map { calendar.startOfDay(for: $0) }
            .sorted()

        // Remove duplicates
        var uniqueDays: [Date] = []
        for date in sortedDates {
            if uniqueDays.last.map({ !calendar.isDate($0, inSameDayAs: date) }) ?? true {
                uniqueDays.append(date)
            }
        }

        var maxStreak = 1
        var currentRun = 1

        for i in 1..<uniqueDays.count {
            let expected = calendar.date(byAdding: .day, value: 1, to: uniqueDays[i - 1])!
            if calendar.isDate(uniqueDays[i], inSameDayAs: expected) {
                currentRun += 1
                maxStreak = max(maxStreak, currentRun)
            } else {
                currentRun = 1
            }
        }

        return maxStreak
    }

    var streakFireEmojis: String {
        let streak = currentStreak
        switch streak {
        case 0: return ""
        case 1...6: return "\u{1F525}"
        case 7...13: return "\u{1F525}\u{1F525}"
        case 14...29: return "\u{1F525}\u{1F525}\u{1F525}"
        default: return "\u{1F525}\u{1F525}\u{1F525}\u{1F525}"
        }
    }

    // MARK: - Mutations

    mutating func toggleToday() {
        let calendar = Calendar.current
        if let index = completionDates.firstIndex(where: { calendar.isDateInToday($0) }) {
            completionDates.remove(at: index)
        } else {
            completionDates.append(Date())
        }
    }

    func completedOn(date: Date) -> Bool {
        let calendar = Calendar.current
        return completionDates.contains { calendar.isDate($0, inSameDayAs: date) }
    }
}
