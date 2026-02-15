import SwiftUI

struct HabitCardView: View {
    let habit: Habit
    let onToggle: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void

    @State private var isAnimating = false
    @State private var checkScale: CGFloat = 1.0

    private var isCompleted: Bool { habit.isCompletedToday }

    var body: some View {
        Button(action: {
            withAnimation(.spring(response: 0.35, dampingFraction: 0.6)) {
                checkScale = 1.3
                onToggle()
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                withAnimation(.spring(response: 0.2, dampingFraction: 0.5)) {
                    checkScale = 1.0
                }
            }
        }) {
            HStack(spacing: 14) {
                // Emoji
                Text(habit.emoji)
                    .font(.system(size: 32))

                // Name and streak info
                VStack(alignment: .leading, spacing: 4) {
                    Text(habit.name)
                        .font(.system(size: 18, weight: .semibold, design: .rounded))
                        .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))
                        .lineLimit(1)

                    if habit.currentStreak > 0 {
                        HStack(spacing: 4) {
                            Text(habit.streakFireEmojis)
                                .font(.system(size: 14))
                            Text("\(habit.currentStreak) day\(habit.currentStreak == 1 ? "" : "s")")
                                .font(.system(size: 14, weight: .medium, design: .rounded))
                                .foregroundColor(streakColor)
                        }
                    } else {
                        Text("Start your streak!")
                            .font(.system(size: 14, weight: .regular, design: .rounded))
                            .foregroundColor(.gray)
                    }
                }

                Spacer()

                // Checkmark button
                ZStack {
                    Circle()
                        .fill(isCompleted ? Color(red: 0.18, green: 0.8, blue: 0.44) : Color.white)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Circle()
                                .stroke(
                                    isCompleted ? Color(red: 0.18, green: 0.8, blue: 0.44) : Color(red: 0.8, green: 0.8, blue: 0.8),
                                    lineWidth: 2
                                )
                        )

                    if isCompleted {
                        Image(systemName: "checkmark")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                .scaleEffect(checkScale)
            }
            .padding(16)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isCompleted ? Color(red: 0.84, green: 0.96, blue: 0.90) : Color(red: 0.96, green: 0.96, blue: 0.96))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(
                        isCompleted ? Color(red: 0.18, green: 0.8, blue: 0.44) : Color(red: 0.8, green: 0.8, blue: 0.8),
                        lineWidth: 2
                    )
            )
            .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(PlainButtonStyle())
        .contextMenu {
            Button(action: onEdit) {
                Label("Edit Habit", systemImage: "pencil")
            }
            Button(role: .destructive, action: onDelete) {
                Label("Delete Habit", systemImage: "trash")
            }
        }
    }

    private var streakColor: Color {
        let streak = habit.currentStreak
        if streak >= 30 {
            return Color(red: 0.95, green: 0.61, blue: 0.07) // Gold
        } else if streak >= 7 {
            return Color(red: 0.90, green: 0.49, blue: 0.13) // Orange
        } else {
            return Color(red: 0.4, green: 0.4, blue: 0.4) // Gray
        }
    }
}
