package com.athreya.mathworkout.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Progress message types
 */
sealed class ProgressMessage(
    val text: String,
    val icon: ImageVector,
    val color: Color
) {
    data class Improving(val improvement: String) : ProgressMessage(
        text = "You're improving! $improvement faster than your average",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF4CAF50)
    )
    
    data class BestTime(val time: String) : ProgressMessage(
        text = "🎉 New personal best! Completed in $time",
        icon = Icons.Default.EmojiEvents,
        color = Color(0xFFFFD700)
    )
    
    data class BestScore(val score: Int) : ProgressMessage(
        text = "🏆 New high score: $score points!",
        icon = Icons.Default.Star,
        color = Color(0xFFFFD700)
    )
    
    data class StreakMilestone(val days: Int) : ProgressMessage(
        text = "🔥 $days day streak! Keep it up!",
        icon = Icons.Default.Whatshot,
        color = Color(0xFFFF5722)
    )
    
    data class AboveAverage(val percentage: Int) : ProgressMessage(
        text = "👏 $percentage% above your average!",
        icon = Icons.Default.ThumbUp,
        color = Color(0xFF2196F3)
    )
    
    object Perfect : ProgressMessage(
        text = "⭐ Perfect! No mistakes!",
        icon = Icons.Default.CheckCircle,
        color = Color(0xFF4CAF50)
    )
    
    data class SpeedDemon(val multiplier: Float) : ProgressMessage(
        text = "⚡ Speed demon! ${multiplier}x time bonus",
        icon = Icons.Default.Bolt,
        color = Color(0xFFFFC107)
    )
    
    data class Encouraging(val customMessage: String) : ProgressMessage(
        text = customMessage,
        icon = Icons.Default.SentimentSatisfied,
        color = Color(0xFF9C27B0)
    )
}

/**
 * Animated progress message card
 */
@Composable
fun ProgressMessageCard(
    message: ProgressMessage,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scale = rememberPulseAnimation()

    // Auto-dismiss after 4 seconds
    LaunchedEffect(message) {
        delay(4000)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (message is ProgressMessage.BestScore || message is ProgressMessage.BestTime) scale.value else 1f),
        colors = CardDefaults.cardColors(
            containerColor = message.color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = message.icon,
                contentDescription = null,
                tint = message.color,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = message.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Generate progress messages based on game results
 */
fun generateProgressMessages(
    score: Int,
    averageScore: Int,
    bestScore: Int,
    timeInSeconds: Float,
    averageTime: Float,
    bestTime: Float,
    wrongAttempts: Int,
    timeMultiplier: Float,
    streakDays: Int,
    isNewRecord: Boolean
): List<ProgressMessage> {
    val messages = mutableListOf<ProgressMessage>()

    // New high score
    if (isNewRecord && score > bestScore) {
        messages.add(ProgressMessage.BestScore(score))
    }

    // New best time
    if (timeInSeconds < bestTime && bestTime > 0) {
        messages.add(ProgressMessage.BestTime(formatTime(timeInSeconds.toLong() * 1000)))
    }

    // Perfect game
    if (wrongAttempts == 0) {
        messages.add(ProgressMessage.Perfect)
    }

    // Speed demon (high time multiplier)
    if (timeMultiplier >= 2.0f) {
        messages.add(ProgressMessage.SpeedDemon(timeMultiplier))
    }

    // Above average performance
    if (averageScore > 0 && score > averageScore) {
        val percentage = ((score - averageScore) * 100 / averageScore).coerceAtMost(999)
        if (percentage >= 20) {
            messages.add(ProgressMessage.AboveAverage(percentage))
        }
    }

    // Improving time
    if (averageTime > 0 && timeInSeconds < averageTime) {
        val improvementSeconds = averageTime - timeInSeconds
        val improvementText = when {
            improvementSeconds >= 5 -> "${improvementSeconds.toInt()}s"
            else -> String.format("%.1fs", improvementSeconds)
        }
        messages.add(ProgressMessage.Improving(improvementText))
    }

    // Streak milestones
    when (streakDays) {
        7, 14, 30, 50, 100 -> messages.add(ProgressMessage.StreakMilestone(streakDays))
    }

    // Encouraging messages for lower performance
    if (messages.isEmpty()) {
        val encouragingMessages = listOf(
            "Keep practicing! Every game makes you better! 💪",
            "You're on the right track! 🎯",
            "Great effort! Try to beat this score next time! 🚀",
            "Nice work! You're building your skills! 📈"
        )
        messages.add(ProgressMessage.Encouraging(encouragingMessages.random()))
    }

    return messages
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}
