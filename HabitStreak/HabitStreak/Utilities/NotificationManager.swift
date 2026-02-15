import Foundation
import UserNotifications

class NotificationManager: ObservableObject {
    static let shared = NotificationManager()

    @Published var isAuthorized = false

    private let dailyReminderIdentifier = "daily_habit_reminder"
    private let streakWarningIdentifier = "streak_warning"

    func requestAuthorization(completion: @escaping (Bool) -> Void = { _ in }) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            DispatchQueue.main.async {
                self.isAuthorized = granted
                completion(granted)
            }
        }
    }

    func checkAuthorizationStatus() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                self.isAuthorized = settings.authorizationStatus == .authorized
            }
        }
    }

    // MARK: - Daily Reminder

    func scheduleDailyReminder(at hour: Int, minute: Int) {
        cancelDailyReminder()

        let content = UNMutableNotificationContent()
        content.title = "Time to build your habits!"
        content.body = "Don't forget to check off your habits today \u{1F4AA}"
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(
            identifier: dailyReminderIdentifier,
            content: content,
            trigger: trigger
        )

        UNUserNotificationCenter.current().add(request)
    }

    func cancelDailyReminder() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(
            withIdentifiers: [dailyReminderIdentifier]
        )
    }

    // MARK: - Streak Warning

    func scheduleStreakWarning(uncompletedCount: Int) {
        cancelStreakWarning()

        guard uncompletedCount > 0 else { return }

        let content = UNMutableNotificationContent()
        content.title = "Don't break your streak! \u{1F525}"
        content.body = "You have \(uncompletedCount) habit\(uncompletedCount == 1 ? "" : "s") left today"
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = 21 // 9 PM
        dateComponents.minute = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
        let request = UNNotificationRequest(
            identifier: streakWarningIdentifier,
            content: content,
            trigger: trigger
        )

        UNUserNotificationCenter.current().add(request)
    }

    func cancelStreakWarning() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(
            withIdentifiers: [streakWarningIdentifier]
        )
    }

    func cancelAll() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
}
