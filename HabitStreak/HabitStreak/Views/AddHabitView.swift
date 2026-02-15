import SwiftUI

struct AddHabitView: View {
    @ObservedObject var viewModel: HabitViewModel
    @Environment(\.dismiss) var dismiss

    @State private var habitName: String = ""
    @State private var selectedEmoji: String = "\u{2B50}"
    @State private var showDuplicateWarning = false

    private let emojiOptions = [
        "\u{1F4AA}", "\u{1F4DA}", "\u{1F3C3}", "\u{1F4A7}", "\u{1F9D8}", "\u{270D}\u{FE0F}", "\u{1F3AF}",
        "\u{1F3A8}", "\u{1F3B5}", "\u{1F331}", "\u{1F525}", "\u{2B50}", "\u{1F4A1}", "\u{2764}\u{FE0F}",
        "\u{1F6B4}", "\u{1F4DD}", "\u{1F34E}", "\u{1F6B6}", "\u{1F4BB}", "\u{1F60A}"
    ]

    private var canSave: Bool {
        let trimmed = habitName.trimmingCharacters(in: .whitespacesAndNewlines)
        return !trimmed.isEmpty && trimmed.count <= 50 && !showDuplicateWarning
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Habit Name Section
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Habit Name")
                            .font(.system(size: 16, weight: .semibold, design: .rounded))
                            .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

                        TextField("e.g., Read for 30 minutes", text: $habitName)
                            .font(.system(size: 17, design: .rounded))
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color(red: 0.96, green: 0.96, blue: 0.96))
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(red: 0.85, green: 0.85, blue: 0.85), lineWidth: 1)
                            )
                            .onChange(of: habitName) { _ in
                                showDuplicateWarning = viewModel.hasDuplicateName(habitName)
                            }

                        if showDuplicateWarning {
                            Text("A habit with this name already exists")
                                .font(.system(size: 13, design: .rounded))
                                .foregroundColor(.red)
                        }

                        Text("\(habitName.count)/50")
                            .font(.system(size: 13, design: .rounded))
                            .foregroundColor(habitName.count > 50 ? .red : .gray)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }

                    // Emoji Picker Section
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Choose Emoji")
                            .font(.system(size: 16, weight: .semibold, design: .rounded))
                            .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

                        // Preview
                        HStack(spacing: 12) {
                            Text(selectedEmoji)
                                .font(.system(size: 40))
                            Text(habitName.isEmpty ? "Your Habit" : habitName)
                                .font(.system(size: 18, weight: .medium, design: .rounded))
                                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))
                                .lineLimit(1)
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color(red: 0.84, green: 0.96, blue: 0.90))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color(red: 0.18, green: 0.8, blue: 0.44), lineWidth: 2)
                        )

                        // Emoji Grid
                        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7), spacing: 12) {
                            ForEach(emojiOptions, id: \.self) { emoji in
                                Button(action: {
                                    withAnimation(.spring(response: 0.2, dampingFraction: 0.6)) {
                                        selectedEmoji = emoji
                                    }
                                }) {
                                    Text(emoji)
                                        .font(.system(size: 30))
                                        .frame(width: 44, height: 44)
                                        .background(
                                            RoundedRectangle(cornerRadius: 10)
                                                .fill(selectedEmoji == emoji ? Color.blue.opacity(0.15) : Color.clear)
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(selectedEmoji == emoji ? Color.blue : Color.clear, lineWidth: 2)
                                        )
                                        .scaleEffect(selectedEmoji == emoji ? 1.1 : 1.0)
                                }
                            }
                        }
                        .padding(12)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color(red: 0.96, green: 0.96, blue: 0.96))
                        )
                    }
                }
                .padding(20)
            }
            .background(Color.white)
            .navigationTitle("Add Habit")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(.gray)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        viewModel.addHabit(name: habitName, emoji: selectedEmoji)
                        dismiss()
                    }
                    .disabled(!canSave)
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(canSave ? Color(red: 0.2, green: 0.6, blue: 0.86) : .gray)
                }
            }
        }
    }
}
