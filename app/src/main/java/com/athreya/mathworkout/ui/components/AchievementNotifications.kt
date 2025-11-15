package com.athreya.mathworkout.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.Achievement
import com.athreya.mathworkout.data.Rank
import kotlinx.coroutines.delay

/**
 * Animated notification popup for achievement unlocks
 */
@Composable
fun AchievementUnlockNotification(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    
    // Trigger animations
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        showConfetti = true
        delay(3000) // Show for 3 seconds
        visible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti animation
        if (showConfetti) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Achievement card with animations
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = scaleOut(targetScale = 0.8f) + fadeOut()
        ) {
            AchievementCard(achievement = achievement)
        }
    }
}

/**
 * Animated notification popup for rank up
 */
@Composable
fun RankUpNotification(
    newRank: Rank,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        showConfetti = true
        delay(3500)
        visible = false
        delay(300)
        onDismiss()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (showConfetti) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize(),
                colors = listOf(newRank.color, newRank.color.copy(alpha = 0.7f), Color.White)
            )
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            RankUpCard(rank = newRank)
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎉 Achievement Unlocked! 🎉",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            // Animated icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconName,
                    fontSize = 56.sp
                )
            }
            
            Text(
                text = achievement.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = achievement.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            if (achievement.xpReward > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankUpCard(rank: Rank) {
    val scale by rememberInfiniteTransition(label = "rank_scale").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rank_icon_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = rank.color.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "⭐ RANK UP! ⭐",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = rank.color,
                textAlign = TextAlign.Center
            )
            
            // Animated rank icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(rank.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.icon,
                    fontSize = 64.sp
                )
            }
            
            Text(
                text = rank.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = rank.color,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = rank.description,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = rank.color.copy(alpha = 0.3f)
            )
            
            Text(
                text = "Keep pushing your limits!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Observer component that displays achievement notifications
 */
@Composable
fun AchievementNotificationObserver(
    achievementManager: com.athreya.mathworkout.data.AchievementManager,
    modifier: Modifier = Modifier
) {
    val newlyUnlockedAchievements by achievementManager.newlyUnlockedAchievements.collectAsState()
    var currentAchievement by remember { mutableStateOf<Achievement?>(null) }
    var currentRank by remember { mutableStateOf<Rank?>(null) }
    var previousRank by remember { mutableStateOf<Rank?>(null) }
    var showingNotification by remember { mutableStateOf(false) }
    
    // Track rank changes
    LaunchedEffect(Unit) {
        previousRank = achievementManager.getCurrentRank()
    }
    
    // Check for rank ups
    LaunchedEffect(achievementManager.getTotalPoints()) {
        val newRank = achievementManager.getCurrentRank()
        if (previousRank != null && newRank.id != previousRank?.id && !showingNotification) {
            currentRank = newRank
            showingNotification = true
        }
        previousRank = newRank
    }
    
    // Show achievement notifications (only if not already showing one)
    LaunchedEffect(newlyUnlockedAchievements) {
        if (newlyUnlockedAchievements.isNotEmpty() && !showingNotification) {
            currentAchievement = newlyUnlockedAchievements.first()
            showingNotification = true
        }
    }
    
    // Display achievement unlock notification (priority over rank up)
    if (currentAchievement != null && showingNotification) {
        AchievementUnlockNotification(
            achievement = currentAchievement!!,
            onDismiss = {
                currentAchievement = null
                showingNotification = false
                achievementManager.clearNewlyUnlockedAchievements()
            }
        )
    } else if (currentRank != null && showingNotification) {
        // Display rank up notification only if no achievement notification
        RankUpNotification(
            newRank = currentRank!!,
            onDismiss = {
                currentRank = null
                showingNotification = false
            }
        )
    }
}
