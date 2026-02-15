import SwiftUI

struct CalendarView: View {
    @ObservedObject var viewModel: HabitViewModel
    @Environment(\.dismiss) var dismiss
    @State private var displayedMonth: Date = Date()
    @State private var selectedDate: Date? = nil

    private let weekdays = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"]
    private let columns = Array(repeating: GridItem(.flexible()), count: 7)

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    calendarHeader
                    calendarGrid
                    if let date = selectedDate {
                        dayDetailView(for: date)
                    }
                    statsSection
                }
                .padding(20)
            }
            .background(Color.white)
            .navigationTitle("History")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { dismiss() }) {
                        HStack(spacing: 4) {
                            Image(systemName: "chevron.left")
                            Text("Back")
                        }
                        .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.86))
                    }
                }
            }
        }
    }

    // MARK: - Calendar Header

    private var calendarHeader: some View {
        HStack {
            Button(action: { changeMonth(by: -1) }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.86))
                    .frame(width: 44, height: 44)
            }

            Spacer()

            Text(DateHelper.monthYearString(for: displayedMonth))
                .font(.system(size: 20, weight: .bold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

            Spacer()

            Button(action: { changeMonth(by: 1) }) {
                Image(systemName: "chevron.right")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(canGoForward ? Color(red: 0.2, green: 0.6, blue: 0.86) : .gray)
                    .frame(width: 44, height: 44)
            }
            .disabled(!canGoForward)
        }
    }

    private var canGoForward: Bool {
        let calendar = Calendar.current
        let currentMonth = calendar.dateComponents([.year, .month], from: Date())
        let displayed = calendar.dateComponents([.year, .month], from: displayedMonth)
        if let current = calendar.date(from: currentMonth),
           let shown = calendar.date(from: displayed) {
            return shown < current
        }
        return false
    }

    // MARK: - Calendar Grid

    private var calendarGrid: some View {
        VStack(spacing: 8) {
            // Weekday headers
            LazyVGrid(columns: columns, spacing: 8) {
                ForEach(weekdays, id: \.self) { day in
                    Text(day)
                        .font(.system(size: 12, weight: .semibold, design: .rounded))
                        .foregroundColor(.gray)
                        .frame(maxWidth: .infinity)
                }
            }

            // Day cells
            LazyVGrid(columns: columns, spacing: 8) {
                // Empty cells for offset
                let offset = DateHelper.firstWeekdayOfMonth(for: displayedMonth) - 1
                ForEach(0..<offset, id: \.self) { _ in
                    Text("")
                        .frame(height: 44)
                }

                // Day cells
                let daysCount = DateHelper.daysInMonth(for: displayedMonth)
                ForEach(1...daysCount, id: \.self) { day in
                    let date = DateHelper.dateFor(day: day, inMonthOf: displayedMonth)
                    let status = viewModel.dayStatus(date: date)
                    let isToday = Calendar.current.isDateInToday(date)
                    let isSelected = selectedDate.map { Calendar.current.isDate($0, inSameDayAs: date) } ?? false

                    Button(action: {
                        if status != .future {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                if isSelected {
                                    selectedDate = nil
                                } else {
                                    selectedDate = date
                                }
                            }
                        }
                    }) {
                        VStack(spacing: 2) {
                            Text("\(day)")
                                .font(.system(size: 16, weight: isToday ? .bold : .regular, design: .rounded))
                                .foregroundColor(dayTextColor(status: status, isToday: isToday))

                            Circle()
                                .fill(dayDotColor(status: status))
                                .frame(width: 6, height: 6)
                                .opacity(status == .future ? 0 : 1)
                        }
                        .frame(width: 40, height: 44)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(dayBackground(status: status, isToday: isToday, isSelected: isSelected))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(isSelected ? Color(red: 0.2, green: 0.6, blue: 0.86) : Color.clear, lineWidth: 2)
                        )
                    }
                    .disabled(status == .future)
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(red: 0.98, green: 0.98, blue: 0.98))
        )
    }

    // MARK: - Day Detail

    private func dayDetailView(for date: Date) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            let formatter = DateFormatter()
            let _ = formatter.dateFormat = "EEEE, MMM d"

            Text(formatter.string(from: date))
                .font(.system(size: 18, weight: .bold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

            ForEach(viewModel.habits) { habit in
                let calendar = Calendar.current
                let wasActive = calendar.startOfDay(for: date) >= calendar.startOfDay(for: habit.createdDate)

                if wasActive {
                    HStack(spacing: 10) {
                        Text(habit.emoji)
                            .font(.system(size: 20))
                        Text(habit.name)
                            .font(.system(size: 16, weight: .medium, design: .rounded))
                            .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))
                        Spacer()
                        if habit.completedOn(date: date) {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(Color(red: 0.18, green: 0.8, blue: 0.44))
                                .font(.system(size: 20))
                        } else {
                            Image(systemName: "circle")
                                .foregroundColor(.gray)
                                .font(.system(size: 20))
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(red: 0.98, green: 0.98, blue: 0.98))
        )
        .transition(.opacity.combined(with: .move(edge: .top)))
    }

    // MARK: - Stats Section

    private var statsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Stats This Month")
                .font(.system(size: 20, weight: .bold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

            VStack(spacing: 10) {
                statRow(
                    icon: "checkmark.circle.fill",
                    color: Color(red: 0.18, green: 0.8, blue: 0.44),
                    label: "Perfect days",
                    value: "\(viewModel.perfectDays(for: displayedMonth))"
                )

                statRow(
                    icon: "chart.bar.fill",
                    color: Color(red: 0.2, green: 0.6, blue: 0.86),
                    label: "Completion rate",
                    value: "\(Int(viewModel.completionRate(for: displayedMonth) * 100))%"
                )

                statRow(
                    icon: "flame.fill",
                    color: Color(red: 0.90, green: 0.49, blue: 0.13),
                    label: "Best streak",
                    value: "\(viewModel.bestStreakThisMonth(for: displayedMonth)) days"
                )
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(Color(red: 0.98, green: 0.98, blue: 0.98))
        )
    }

    private func statRow(icon: String, color: Color, label: String, value: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .foregroundColor(color)
                .font(.system(size: 18))
                .frame(width: 28)

            Text(label)
                .font(.system(size: 16, weight: .medium, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

            Spacer()

            Text(value)
                .font(.system(size: 16, weight: .semibold, design: .rounded))
                .foregroundColor(Color(red: 0.4, green: 0.4, blue: 0.4))
        }
        .padding(.vertical, 4)
    }

    // MARK: - Helpers

    private func changeMonth(by value: Int) {
        withAnimation(.easeInOut(duration: 0.2)) {
            if let newDate = Calendar.current.date(byAdding: .month, value: value, to: displayedMonth) {
                displayedMonth = newDate
                selectedDate = nil
            }
        }
    }

    private func dayTextColor(status: DayCompletionStatus, isToday: Bool) -> Color {
        switch status {
        case .future:
            return Color(red: 0.8, green: 0.8, blue: 0.8)
        default:
            return isToday ? Color(red: 0.2, green: 0.6, blue: 0.86) : Color(red: 0.12, green: 0.12, blue: 0.12)
        }
    }

    private func dayDotColor(status: DayCompletionStatus) -> Color {
        switch status {
        case .allCompleted:
            return Color(red: 0.18, green: 0.8, blue: 0.44)
        case .someCompleted:
            return Color(red: 0.18, green: 0.8, blue: 0.44).opacity(0.4)
        case .noneCompleted:
            return Color(red: 0.8, green: 0.8, blue: 0.8)
        case .future, .noHabits:
            return .clear
        }
    }

    private func dayBackground(status: DayCompletionStatus, isToday: Bool, isSelected: Bool) -> Color {
        if isSelected {
            return Color(red: 0.2, green: 0.6, blue: 0.86).opacity(0.1)
        }
        if isToday {
            return Color(red: 0.2, green: 0.6, blue: 0.86).opacity(0.08)
        }
        if status == .allCompleted {
            return Color(red: 0.18, green: 0.8, blue: 0.44).opacity(0.08)
        }
        return .clear
    }
}
