import SwiftUI

struct WelcomeView: View {
    @ObservedObject var viewModel: HabitViewModel
    @Binding var showWelcome: Bool
    @State private var opacity: Double = 0
    @State private var offset: CGFloat = 30

    var isFirstLaunch: Bool

    var body: some View {
        ZStack {
            // Gradient background
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.93, green: 0.95, blue: 1.0),
                    Color(red: 0.85, green: 0.88, blue: 1.0)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 20) {
                Spacer()

                if isFirstLaunch {
                    firstLaunchContent
                } else {
                    returningUserContent
                }

                Spacer()

                Button(action: {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        if isFirstLaunch {
                            viewModel.markFirstLaunchDone()
                        }
                        viewModel.updateLastOpenDate()
                        showWelcome = false
                    }
                }) {
                    Text(isFirstLaunch ? "Get Started" : "Continue")
                        .font(.system(size: 18, weight: .semibold, design: .rounded))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(red: 0.2, green: 0.6, blue: 0.86))
                        .cornerRadius(16)
                }
                .padding(.horizontal, 40)
                .padding(.bottom, 60)
            }
            .opacity(opacity)
            .offset(y: offset)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.6)) {
                opacity = 1
                offset = 0
            }

            // Auto-dismiss for returning users
            if !isFirstLaunch {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        viewModel.updateLastOpenDate()
                        showWelcome = false
                    }
                }
            }
        }
    }

    // MARK: - First Launch Content

    private var firstLaunchContent: some View {
        VStack(spacing: 16) {
            Text("\u{1F525}")
                .font(.system(size: 80))

            Text("Welcome to Streaks!")
                .font(.system(size: 36, weight: .bold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))
                .multilineTextAlignment(.center)

            Text("Build better habits,\none day at a time")
                .font(.system(size: 18, weight: .regular, design: .rounded))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
        }
    }

    // MARK: - Returning User Content

    private var returningUserContent: some View {
        VStack(spacing: 16) {
            Text(DateHelper.timeOfDay().greeting)
                .font(.system(size: 32, weight: .bold, design: .rounded))
                .foregroundColor(Color(red: 0.12, green: 0.12, blue: 0.12))
                .multilineTextAlignment(.center)

            if viewModel.habits.isEmpty {
                Text("Ready to start tracking?")
                    .font(.system(size: 20, weight: .regular, design: .rounded))
                    .foregroundColor(.gray)
            } else {
                let completed = viewModel.completedTodayCount
                let remaining = viewModel.remainingTodayCount

                if viewModel.allCompletedToday {
                    Text("All done for today! \u{1F389}")
                        .font(.system(size: 20, weight: .medium, design: .rounded))
                        .foregroundColor(Color(red: 0.18, green: 0.8, blue: 0.44))
                } else if completed > 0 {
                    Text("\(completed) habit\(completed == 1 ? "" : "s") completed\n\(remaining) more to go!")
                        .font(.system(size: 20, weight: .regular, design: .rounded))
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                } else {
                    Text("You're on a roll!\nLet's keep it going")
                        .font(.system(size: 20, weight: .regular, design: .rounded))
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                }
            }
        }
    }
}
