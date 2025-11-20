package com.athreya.mathworkout.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.DailyChallenge
import com.athreya.mathworkout.data.GameMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Daily Challenge Screen - Shows today's special challenge with bonus rewards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    todaysChallenge: DailyChallenge?,
    completedChallenges: List<DailyChallenge>,
    onStartChallenge: (DailyChallenge) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Challenge Card
            item {
                todaysChallenge?.let { challenge ->
                    TodaysChallengeCard(
                        challenge = challenge,
                        currentStreak = calculateStreak(completedChallenges),
                        onStartClick = { onStartChallenge(challenge) }
                    )
                } ?: run {
                    LoadingChallengeCard()
                }
            }
            
            // Stats Card
            item {
                ChallengeStatsCard(
                    completedCount = completedChallenges.size,
                    currentStreak = calculateStreak(completedChallenges)
                )
            }
            
            // Completed Challenges History
            item {
                Text(
                    text = "Challenge History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (completedChallenges.isEmpty()) {
                item {
                    EmptyHistoryCard()
                }
            } else {
                items(completedChallenges) { challenge ->
                    CompletedChallengeCard(challenge)
                }
            }
        }
    }
}

@Composable
fun TodaysChallengeCard(
    challenge: DailyChallenge,
    currentStreak: Int,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate actual streak multiplier
    val baseMultiplier = 2.7f
    val streakBonus = when {
        currentStreak >= 30 -> 1.3f
        currentStreak >= 14 -> 1.2f
        currentStreak >= 7 -> 1.15f
        currentStreak >= 3 -> 1.1f
        else -> 1.0f
    }
    val actualMultiplier = baseMultiplier * streakBonus
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.completed) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = glowAlpha)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (challenge.completed) 2.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with trophy icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (challenge.completed) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (challenge.completed) Color(0xFF4CAF50) else Color(0xFFFFD700)
                )
                Text(
                    text = if (challenge.completed) "Challenge Complete!" else "Today's Challenge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (challenge.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
            
            Divider()
            
            // Challenge details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChallengeDetail(
                    icon = Icons.Default.SportsEsports,
                    label = "Mode",
                    value = formatGameMode(challenge.gameMode)
                )
                ChallengeDetail(
                    icon = Icons.Default.Speed,
                    label = "Difficulty",
                    value = challenge.difficulty
                )
            }
            
            // Bonus multiplier badge - show actual streak multiplier
            BonusMultiplierBadge(multiplier = actualMultiplier)
            
            // Start/Completed button
            if (!challenge.completed) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Challenge", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Show completion stats
                CompletionStats(
                    timeTaken = challenge.timeTaken,
                    wrongAttempts = challenge.wrongAttempts
                )
            }
        }
    }
}

@Composable
fun ChallengeDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BonusMultiplierBadge(
    multiplier: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFF9800).copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF9800))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${String.format("%.1f", multiplier)}x Bonus Points!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CompletionStats(
    timeTaken: Long,
    wrongAttempts: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Timer,
                label = "Time",
                value = formatTime(timeTaken)
            )
            StatItem(
                icon = Icons.Default.Error,
                label = "Mistakes",
                value = wrongAttempts.toString()
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChallengeStatsCard(
    completedCount: Int,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsItem(
                icon = Icons.Default.EmojiEvents,
                label = "Completed",
                value = completedCount.toString(),
                color = Color(0xFFFFD700)
            )
            StatsItem(
                icon = Icons.Default.LocalFireDepartment,
                label = "Streak",
                value = currentStreak.toString(),
                color = Color(0xFFFF5722)
            )
        }
    }
}

@Composable
fun StatsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun CompletedChallengeCard(
    challenge: DailyChallenge,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = challenge.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatGameMode(challenge.gameMode)} • ${challenge.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTime(challenge.timeTaken),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = if (challenge.wrongAttempts == 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        if (challenge.wrongAttempts == 0) Icons.Default.Check else Icons.Default.Close,
                        null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.History,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No challenges completed yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Complete your first daily challenge to start your streak!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingChallengeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

// Helper functions
private fun formatGameMode(gameMode: String): String {
    return when (gameMode) {
        "Addition" -> "Addition"
        "Subtraction" -> "Subtraction"
        "Multiplication" -> "Multiplication"
        "Division" -> "Division"
        "TestMe" -> "Test Me"
        "BrainTeaser" -> "Brain Teaser"
        "Sudoku" -> "Sudoku"
        else -> gameMode
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${seconds}s"
    }
}

private fun calculateStreak(completedChallenges: List<DailyChallenge>): Int {
    if (completedChallenges.isEmpty()) return 0
    
    val sorted = completedChallenges.sortedByDescending { it.date }
    var streak = 0
    val today = DailyChallenge.getTodayDate()
    val calendar = Calendar.getInstance()
    
    for (challenge in sorted) {
        val expectedDate = "${calendar.get(Calendar.YEAR)}-" +
                "${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-" +
                "${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
        
        if (challenge.date == expectedDate) {
            streak++
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            break
        }
    }
    
    return streak
}
