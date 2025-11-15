package com.athreya.mathworkout.ui.components

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Animated counter that counts up from 0 to the target value
 * Creates a satisfying animation effect for scores
 */
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    duration: Int = 1500, // Duration in milliseconds
    prefix: String = "",
    suffix: String = ""
) {
    var animatedCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(count) {
        val animationSpec = tween<Float>(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        )
        
        animate(
            initialValue = 0f,
            targetValue = count.toFloat(),
            animationSpec = animationSpec
        ) { value, _ ->
            animatedCount = value.toInt()
        }
    }
    
    Text(
        text = "$prefix$animatedCount$suffix",
        modifier = modifier,
        style = style,
        color = color
    )
}

/**
 * Animated counter with spring animation
 * Bounces slightly at the end for extra impact
 */
@Composable
fun AnimatedCounterSpring(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedCount = remember { Animatable(0f) }
    
    LaunchedEffect(count) {
        animatedCount.animateTo(
            targetValue = count.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Text(
        text = "$prefix${animatedCount.value.toInt()}$suffix",
        modifier = modifier,
        style = style,
        color = color
    )
}
