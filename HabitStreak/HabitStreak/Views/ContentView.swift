import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = HabitViewModel()
    @State private var showWelcome = true

    var body: some View {
        ZStack {
            MainView(viewModel: viewModel)

            if showWelcome {
                WelcomeView(
                    viewModel: viewModel,
                    showWelcome: $showWelcome,
                    isFirstLaunch: viewModel.isFirstLaunch
                )
                .transition(.opacity)
                .zIndex(1)
            }
        }
        .animation(.easeInOut(duration: 0.4), value: showWelcome)
    }
}
