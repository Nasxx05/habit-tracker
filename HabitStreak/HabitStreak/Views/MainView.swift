import SwiftUI

struct MainView: View {
    @ObservedObject var viewModel: HabitViewModel
    @State private var showAddHabit = false
    @State private var showCalendar = false
    @State private var editingHabit: Habit? = nil
    @State private var showDeleteConfirmation = false
    @State private var habitToDelete: UUID? = nil
    @State private var showConfetti = false

    var body: some View {
        NavigationView {
            ZStack {
                Color.white.ignoresSafeArea()

                VStack(spacing: 0) {
                    headerSection
                    habitsList
                    addButton
                }

                // Confetti overlay
                if showConfetti || viewModel.showMilestoneConfetti {
                    ConfettiView()
                        .ignoresSafeArea()
                        .allowsHitTesting(false)
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                                withAnimation {
                                    showConfetti = false
                                    viewModel.showMilestoneConfetti = false
                                }
                            }
                        }
                }

                // Milestone toast
                if viewModel.showMilestoneConfetti {
                    VStack {
                        Spacer()
                        milestoneToast
                            .padding(.bottom, 100)
                            .transition(.move(edge: .bottom).combined(with: .opacity))
                    }
                    .animation(.spring(response: 0.5, dampingFraction: 0.7), value: viewModel.showMilestoneConfetti)
                }
            }
            .navigationBarHidden(true)
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .sheet(isPresented: $showAddHabit) {
            AddHabitView(viewModel: viewModel)
        }
        .sheet(isPresented: $showCalendar) {
            CalendarView(viewModel: viewModel)
        }
        .sheet(item: $editingHabit) { habit in
            EditHabitView(viewModel: viewModel, habit: habit)
        }
        .alert("Delete Habit?", isPresented: $showDeleteConfirmation) {
            Button("Cancel", role: .cancel) {
                habitToDelete = nil
            }
            Button("Delete", role: .destructive) {
                if let id = habitToDelete {
                    withAnimation {
                        viewModel.deleteHabit(id: id)
                    }
                }
                habitToDelete = nil
            }
        } message: {
            Text("This will permanently delete this habit and all its history.")
        }
        .onChange(of: viewModel.showMilestoneConfetti) { newValue in
            if newValue {
                showConfetti = true
            }
        }
    }

    // MARK: - Header

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("Habit Streaks")
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

                Spacer()

                Button(action: { showCalendar = true }) {
                    Image(systemName: "calendar")
                        .font(.system(size: 22))
                        .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.86))
                }
            }

            HStack(spacing: 6) {
                Text("\u{1F4C5}")
                    .font(.system(size: 16))
                Text(DateHelper.formattedDate())
                    .font(.system(size: 16, weight: .regular, design: .rounded))
                    .foregroundColor(.gray)
            }

            Text(DateHelper.dailyMotivationalMessage())
                .font(.system(size: 15, weight: .medium, design: .rounded))
                .foregroundColor(Color(red: 0.4, green: 0.4, blue: 0.4))
                .padding(.top, 2)
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
        .padding(.bottom, 12)
    }

    // MARK: - Habits List

    private var habitsList: some View {
        ScrollView {
            if viewModel.habits.isEmpty {
                emptyStateView
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.habits) { habit in
                        HabitCardView(
                            habit: habit,
                            onToggle: {
                                viewModel.toggleHabit(id: habit.id)
                            },
                            onEdit: {
                                editingHabit = habit
                            },
                            onDelete: {
                                habitToDelete = habit.id
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
        }
    }

    // MARK: - Empty State

    private var emptyStateView: some View {
        VStack(spacing: 16) {
            Spacer()
                .frame(height: 60)

            Text("\u{1F331}")
                .font(.system(size: 60))

            Text("No habits yet")
                .font(.system(size: 22, weight: .semibold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))

            Text("Add your first habit to start\nbuilding streaks!")
                .font(.system(size: 16, weight: .regular, design: .rounded))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)

            Spacer()
        }
    }

    // MARK: - Add Button

    private var addButton: some View {
        Button(action: { showAddHabit = true }) {
            HStack {
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .semibold))
                Text("Add New Habit")
                    .font(.system(size: 18, weight: .semibold, design: .rounded))
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(Color(red: 0.2, green: 0.6, blue: 0.86))
            .cornerRadius(14)
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 20)
        .padding(.top, 8)
        .background(
            Color.white
                .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: -4)
        )
    }

    // MARK: - Milestone Toast

    private var milestoneToast: some View {
        Text(viewModel.milestoneMessage)
            .font(.system(size: 20, weight: .bold, design: .rounded))
            .foregroundColor(.white)
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(
                Capsule()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 0.90, green: 0.49, blue: 0.13),
                                Color(red: 0.95, green: 0.61, blue: 0.07)
                            ]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
            )
            .shadow(color: Color(red: 0.90, green: 0.49, blue: 0.13).opacity(0.4), radius: 12, x: 0, y: 4)
    }
}

// MARK: - Edit Habit View

struct EditHabitView: View {
    @ObservedObject var viewModel: HabitViewModel
    let habit: Habit
    @Environment(\.dismiss) var dismiss

    @State private var name: String = ""
    @State private var selectedEmoji: String = ""
    @State private var showDuplicateWarning = false

    private let emojiOptions = [
        "\u{1F4AA}", "\u{1F4DA}", "\u{1F3C3}", "\u{1F4A7}", "\u{1F9D8}", "\u{270D}\u{FE0F}", "\u{1F3AF}",
        "\u{1F3A8}", "\u{1F3B5}", "\u{1F331}", "\u{1F525}", "\u{2B50}", "\u{1F4A1}", "\u{2764}\u{FE0F}",
        "\u{1F6B4}", "\u{1F4DD}", "\u{1F34E}", "\u{1F6B6}", "\u{1F4BB}", "\u{1F60A}"
    ]

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Habit Name")) {
                    TextField("e.g., Read for 30 minutes", text: $name)
                        .font(.system(size: 17, design: .rounded))
                        .onChange(of: name) { _ in
                            showDuplicateWarning = viewModel.hasDuplicateName(name, excludingId: habit.id)
                        }

                    if showDuplicateWarning {
                        Text("A habit with this name already exists")
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }

                Section(header: Text("Choose Emoji")) {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7), spacing: 12) {
                        ForEach(emojiOptions, id: \.self) { emoji in
                            Button(action: { selectedEmoji = emoji }) {
                                Text(emoji)
                                    .font(.system(size: 28))
                                    .padding(6)
                                    .background(
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(selectedEmoji == emoji ? Color.blue.opacity(0.2) : Color.clear)
                                    )
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(selectedEmoji == emoji ? Color.blue : Color.clear, lineWidth: 2)
                                    )
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }

                Section(header: Text("Stats")) {
                    HStack {
                        Text("Current Streak")
                        Spacer()
                        Text("\(habit.currentStreak) days")
                            .foregroundColor(.gray)
                    }
                    HStack {
                        Text("Longest Streak")
                        Spacer()
                        Text("\(habit.longestStreak) days")
                            .foregroundColor(.gray)
                    }
                    HStack {
                        Text("Total Completions")
                        Spacer()
                        Text("\(habit.completionDates.count)")
                            .foregroundColor(.gray)
                    }
                }
            }
            .navigationTitle("Edit Habit")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Save") {
                        viewModel.updateHabit(id: habit.id, name: name, emoji: selectedEmoji)
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || showDuplicateWarning)
                    .font(.system(size: 17, weight: .semibold))
                }
            }
            .onAppear {
                name = habit.name
                selectedEmoji = habit.emoji
            }
        }
    }
}
