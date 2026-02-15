package com.habitstreak.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.habitstreak.app.ui.theme.*
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    var rotation: Float,
    val rotationSpeed: Float,
    var velocityX: Float,
    var velocityY: Float,
    val shapeType: Int // 0 = rect, 1 = circle
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    val colors = listOf(
        SuccessGreen, PrimaryBlue, StreakOrange, Gold,
        ConfettiRed, ConfettiPurple, ConfettiYellow
    )

    var particles by remember {
        mutableStateOf(
            (0 until 60).map {
                Particle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * -0.3f,
                    size = Random.nextFloat() * 8f + 4f,
                    color = colors.random(),
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 10f - 5f,
                    velocityX = Random.nextFloat() * 4f - 2f,
                    velocityY = Random.nextFloat() * 4f + 2f,
                    shapeType = Random.nextInt(2)
                )
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_tick"
    )

    var frameCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(tick) {
        frameCount++
        particles = particles.map { p ->
            p.copy(
                x = p.x + p.velocityX * 0.002f,
                y = p.y + p.velocityY * 0.003f,
                rotation = p.rotation + p.rotationSpeed,
                velocityY = p.velocityY + 0.15f
            )
        }

        if (frameCount > 150) {
            onFinished()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val px = p.x * w
            val py = p.y * h

            if (py < h + 50) {
                val alpha = if (py > h * 0.7f) {
                    ((h - py) / (h * 0.3f)).coerceIn(0f, 1f)
                } else 1f

                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    when (p.shapeType) {
                        0 -> drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(px - p.size / 2, py - p.size / 2),
                            size = Size(p.size, p.size * 1.5f)
                        )
                        else -> drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size / 2,
                            center = Offset(px, py)
                        )
                    }
                }
            }
        }
    }
}
