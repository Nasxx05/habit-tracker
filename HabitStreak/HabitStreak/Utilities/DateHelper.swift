import Foundation

enum TimeOfDay {
    case morning
    case afternoon
    case evening
    case night

    var greeting: String {
        switch self {
        case .morning: return "Good morning! \u{2600}\u{FE0F}"
        case .afternoon: return "Good afternoon! \u{1F44B}"
        case .evening: return "Good evening! \u{1F319}"
        case .night: return "Still up? \u{1F989}"
        }
    }
}

enum DateHelper {
    static func timeOfDay(for date: Date = Date()) -> TimeOfDay {
        let hour = Calendar.current.component(.hour, from: date)
        switch hour {
        case 6..<12: return .morning
        case 12..<18: return .afternoon
        case 18..<24: return .evening
        default: return .night
        }
    }

    static func formattedDate(_ date: Date = Date()) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, MMM d, yyyy"
        return formatter.string(from: date)
    }

    static func monthYearString(for date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: date)
    }

    static func daysInMonth(for date: Date) -> Int {
        let calendar = Calendar.current
        let range = calendar.range(of: .day, in: .month, for: date)!
        return range.count
    }

    static func firstWeekdayOfMonth(for date: Date) -> Int {
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month], from: date)
        components.day = 1
        let firstDay = calendar.date(from: components)!
        // Sunday = 1, Monday = 2, ..., Saturday = 7
        return calendar.component(.weekday, from: firstDay)
    }

    static func dateFor(day: Int, inMonthOf date: Date) -> Date {
        let calendar = Calendar.current
        var components = calendar.dateComponents([.year, .month], from: date)
        components.day = day
        return calendar.date(from: components)!
    }

    static var motivationalMessages: [String] {
        [
            "Keep the streak alive! \u{1F525}",
            "You've got this! \u{1F4AA}",
            "One day at a time! \u{2B50}",
            "Consistency is key! \u{1F3AF}",
            "Build those habits! \u{1F331}",
            "Stay focused! \u{1F4A1}",
            "Progress, not perfection! \u{1F680}"
        ]
    }

    static func dailyMotivationalMessage() -> String {
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 0
        let index = dayOfYear % motivationalMessages.count
        return motivationalMessages[index]
    }
}
