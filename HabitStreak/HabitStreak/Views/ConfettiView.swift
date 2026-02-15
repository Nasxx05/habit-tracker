import SwiftUI

struct ConfettiView: View {
    @State private var particles: [ConfettiParticle] = []
    @State private var animationTimer: Timer?

    private let colors: [Color] = [
        Color(red: 0.18, green: 0.8, blue: 0.44),   // Green
        Color(red: 0.2, green: 0.6, blue: 0.86),     // Blue
        Color(red: 0.90, green: 0.49, blue: 0.13),   // Orange
        Color(red: 0.95, green: 0.61, blue: 0.07),   // Gold
        Color(red: 0.91, green: 0.30, blue: 0.24),   // Red
        Color(red: 0.61, green: 0.35, blue: 0.71),   // Purple
        Color(red: 0.96, green: 0.76, blue: 0.07)    // Yellow
    ]

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                ForEach(particles) { particle in
                    particle.shape
                        .fill(particle.color)
                        .frame(width: particle.size, height: particle.size * particle.aspectRatio)
                        .rotationEffect(.degrees(particle.rotation))
                        .position(x: particle.x, y: particle.y)
                        .opacity(particle.opacity)
                }
            }
            .onAppear {
                createParticles(in: geometry.size)
            }
            .onDisappear {
                animationTimer?.invalidate()
            }
        }
    }

    private func createParticles(in size: CGSize) {
        particles = (0..<50).map { _ in
            ConfettiParticle(
                x: CGFloat.random(in: 0...size.width),
                y: CGFloat.random(in: -100...(-20)),
                size: CGFloat.random(in: 6...12),
                aspectRatio: CGFloat.random(in: 0.5...2.0),
                color: colors.randomElement() ?? .blue,
                rotation: Double.random(in: 0...360),
                rotationSpeed: Double.random(in: -8...8),
                velocityX: CGFloat.random(in: -2...2),
                velocityY: CGFloat.random(in: 2...6),
                opacity: 1.0,
                shapeType: Int.random(in: 0...2)
            )
        }

        // Animate particles falling
        animationTimer = Timer.scheduledTimer(withTimeInterval: 1.0 / 60.0, repeats: true) { timer in
            var allDone = true
            for i in particles.indices {
                particles[i].y += particles[i].velocityY
                particles[i].x += particles[i].velocityX
                particles[i].rotation += particles[i].rotationSpeed
                particles[i].velocityY += 0.1 // gravity
                particles[i].velocityX *= 0.99 // drag

                if particles[i].y > UIScreen.main.bounds.height + 50 {
                    particles[i].opacity = 0
                } else {
                    allDone = false
                    // Fade near bottom
                    let fadeStart = UIScreen.main.bounds.height * 0.7
                    if particles[i].y > fadeStart {
                        let progress = (particles[i].y - fadeStart) / (UIScreen.main.bounds.height - fadeStart)
                        particles[i].opacity = max(0, 1.0 - progress)
                    }
                }
            }

            if allDone {
                timer.invalidate()
            }
        }
    }
}

struct ConfettiParticle: Identifiable {
    let id = UUID()
    var x: CGFloat
    var y: CGFloat
    var size: CGFloat
    var aspectRatio: CGFloat
    var color: Color
    var rotation: Double
    var rotationSpeed: Double
    var velocityX: CGFloat
    var velocityY: CGFloat
    var opacity: Double
    var shapeType: Int

    var shape: AnyShape {
        switch shapeType {
        case 0:
            return AnyShape(Rectangle())
        case 1:
            return AnyShape(Circle())
        default:
            return AnyShape(Capsule())
        }
    }
}

struct AnyShape: Shape {
    private let pathBuilder: (CGRect) -> Path

    init<S: Shape>(_ shape: S) {
        pathBuilder = { rect in
            shape.path(in: rect)
        }
    }

    func path(in rect: CGRect) -> Path {
        pathBuilder(rect)
    }
}
