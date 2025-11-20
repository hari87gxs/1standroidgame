package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athreya.mathworkout.data.social.Challenge
import com.athreya.mathworkout.data.social.ChallengeStatus
import com.athreya.mathworkout.viewmodel.ChallengeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Helper to handle legacy challenges that stored time (ms) as score
 * If score looks like milliseconds (> 1000), convert to points
 * Otherwise, use score as-is (it's already points)
 */
private fun normalizeScore(score: Int?): Int {
    if (score == null) return 0
    // If score is very large (> 1000), it's likely milliseconds from old system
    // Convert using inverse relationship: faster time = more points
    return if (score > 1000) {
        val timeInSeconds = score / 1000.0
        max(0, (10000 - (timeInSeconds * 100)).toInt())
    } else {
        score // Already points
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    challengeViewModel: ChallengeViewModel,
    onNavigateBack: () -> Unit,
    onChallengeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferences = remember { com.athreya.mathworkout.data.UserPreferencesManager(context) }
    val currentUserId = userPreferences.getDeviceId()
    
    val pendingChallenges by challengeViewModel.pendingChallenges.collectAsState()
    val activeChallenges by challengeViewModel.activeChallenges.collectAsState()
    val completedChallenges by challengeViewModel.completedChallenges.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Active", "Completed")
    
    // Sync challenges from Firebase when screen is first displayed
    LaunchedEffect(Unit) {
        android.util.Log.d("ChallengesScreen", "Syncing challenges from Firebase on screen load")
        challengeViewModel.syncChallengesFromFirebase()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenges") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        android.util.Log.d("ChallengesScreen", "Refresh button clicked, syncing challenges")
                        challengeViewModel.syncChallengesFromFirebase()
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh challenges")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> PendingChallengesTab(
                    challenges = pendingChallenges,
                    currentUserId = currentUserId,
                    challengeViewModel = challengeViewModel
                )
                1 -> ActiveChallengesTab(
                    challenges = activeChallenges,
                    currentUserId = currentUserId,
                    onChallengeClick = onChallengeClick
                )
                2 -> CompletedChallengesTab(
                    challenges = completedChallenges,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
fun PendingChallengesTab(
    challenges: List<Challenge>,
    currentUserId: String,
    challengeViewModel: ChallengeViewModel
) {
    if (challenges.isEmpty()) {
        EmptyChallengesView(
            icon = Icons.Default.NotificationsNone,
            message = "No pending challenges",
            subtitle = "Challenges you send or receive will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                val isChallenger = challenge.challengerId == currentUserId
                if (isChallenger) {
                    // Show "Waiting for response" card
                    SentChallengeCard(challenge = challenge)
                } else {
                    // Show "Accept/Decline" card
                    PendingChallengeCard(
                        challenge = challenge,
                        onAccept = {
                            challengeViewModel.acceptChallenge(
                                challengeId = challenge.challengeId,
                                onSuccess = {},
                                onError = {}
                            )
                        },
                        onDecline = {
                            challengeViewModel.declineChallenge(
                                challengeId = challenge.challengeId,
                                onSuccess = {},
                                onError = {}
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveChallengesTab(
    challenges: List<Challenge>,
    currentUserId: String,
    onChallengeClick: (String) -> Unit
) {
    if (challenges.isEmpty()) {
        EmptyChallengesView(
            icon = Icons.Default.EmojiEvents,
            message = "No active challenges",
            subtitle = "Accepted challenges will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                ActiveChallengeCard(
                    challenge = challenge,
                    currentUserId = currentUserId,
                    onClick = { onChallengeClick(challenge.challengeId) }
                )
            }
        }
    }
}

@Composable
fun CompletedChallengesTab(
    challenges: List<Challenge>,
    currentUserId: String
) {
    if (challenges.isEmpty()) {
        EmptyChallengesView(
            icon = Icons.Default.CheckCircleOutline,
            message = "No completed challenges",
            subtitle = "Finished challenges will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges) { challenge ->
                CompletedChallengeCard(
                    challenge = challenge,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
fun SentChallengeCard(
    challenge: Challenge
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Challenge sent to ${challenge.challengedName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Game: ${challenge.gameMode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${challenge.questionCount} questions • ${challenge.difficulty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Waiting for response...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun PendingChallengeCard(
    challenge: Challenge,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    // Load badges for challenger (showing their accomplishments)
    val context = androidx.compose.ui.platform.LocalContext.current
    val badgeManager = remember { com.athreya.mathworkout.data.BadgeManager(com.athreya.mathworkout.data.UserPreferencesManager(context)) }
    val challengerBadges = remember { badgeManager.getUnlockedBadges().take(3) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${challenge.challengerName} challenges you!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Display challenger's badges
                    if (challengerBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        com.athreya.mathworkout.ui.components.BadgeRow(
                            badges = challengerBadges,
                            maxBadges = 3
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Game: ${challenge.gameMode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${challenge.questionCount} questions • ${challenge.difficulty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Icon(
                    Icons.Default.NotificationImportant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, "Decline", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, "Accept", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun ActiveChallengeCard(
    challenge: Challenge,
    currentUserId: String,
    onClick: () -> Unit
) {
    // Determine if current user is the challenger
    val isChallenger = challenge.challengerId == currentUserId
    val myScore = if (isChallenger) challenge.challengerScore else challenge.challengedScore
    val theirScore = if (isChallenger) challenge.challengedScore else challenge.challengerScore
    val opponentName = if (isChallenger) challenge.challengedName else challenge.challengerName
    
    // Normalize scores (handle legacy millisecond scores)
    val myPoints = if (myScore != null) normalizeScore(myScore) else null
    val theirPoints = if (theirScore != null) normalizeScore(theirScore) else null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "vs $opponentName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Game: ${challenge.gameMode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${challenge.questionCount} questions • ${challenge.difficulty}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show scores
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Your Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (myPoints != null) "$myPoints" else "-",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Their Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (theirPoints != null) "$theirPoints" else "-",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Play Now button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play Now")
                    }
                }
                
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun CompletedChallengeCard(
    challenge: Challenge,
    currentUserId: String
) {
    val winnerId = challenge.winnerId
    val isChallenger = challenge.challengerId == currentUserId
    val didIWin = winnerId == currentUserId
    val myScore = if (isChallenger) challenge.challengerScore else challenge.challengedScore
    val theirScore = if (isChallenger) challenge.challengedScore else challenge.challengerScore
    val opponentName = if (isChallenger) challenge.challengedName else challenge.challengerName
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Normalize scores (handle legacy millisecond scores)
    val myPoints = normalizeScore(myScore)
    val theirPoints = normalizeScore(theirScore)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (didIWin) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (didIWin) "🏆 Victory!" else "Lost",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (didIWin) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "vs $opponentName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (didIWin) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "${challenge.gameMode} • ${dateFormat.format(Date(challenge.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (didIWin) 
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$myPoints",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (didIWin) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "vs $theirPoints",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (didIWin) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChallengesView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
