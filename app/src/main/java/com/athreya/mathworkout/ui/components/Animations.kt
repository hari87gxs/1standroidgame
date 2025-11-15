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

/**
 * Shake animation modifier for wrong answers
 */
fun Modifier.shake(enabled: Boolean): Modifier {
    return this.then(
        if (enabled) {
            Modifier // Shake will be handled by animateFloatAsState in the composable
        } else {
            Modifier
        }
    )
}

/**
 * Number pop animation - scales and fades in numbers
 */
@Composable
fun rememberNumberPopAnimation(): Animatable<Float, AnimationVector1D> {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    return animatable
}

/**
 * Pulsing animation for highlighting important elements
 */
@Composable
fun rememberPulseAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
}
