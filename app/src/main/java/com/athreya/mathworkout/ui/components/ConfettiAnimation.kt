package com.athreya.mathworkout.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val rotation: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFFFFD700),
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF95E1D3),
        Color(0xFFF38181)
    ),
    particleCount: Int = 50
) {
    val particles = remember(colors, particleCount) {
        List(particleCount) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = -0.1f,
                color = colors.random(),
                rotation = Random.nextFloat() * 360f,
                velocityX = (Random.nextFloat() - 0.5f) * 2f,
                velocityY = Random.nextFloat() * 2f + 1f,
                size = Random.nextFloat() * 10f + 5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val progress = (animationProgress + particle.startY) % 1f
            val x = width * particle.startX + particle.velocityX * progress * width * 0.3f
            val y = height * progress
            val currentRotation = particle.rotation + progress * 720f

            rotate(currentRotation, pivot = Offset(x, y)) {
                // Draw confetti piece as a small rectangle
                val path = Path().apply {
                    val halfSize = particle.size / 2f
                    moveTo(x - halfSize, y - halfSize * 2)
                    lineTo(x + halfSize, y - halfSize * 2)
                    lineTo(x + halfSize, y + halfSize * 2)
                    lineTo(x - halfSize, y + halfSize * 2)
                    close()
                }
                drawPath(
                    path = path,
                    color = particle.color.copy(alpha = 1f - progress)
                )
            }
        }
    }
}
