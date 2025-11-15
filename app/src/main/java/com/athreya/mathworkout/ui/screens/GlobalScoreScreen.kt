package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.athreya.mathworkout.data.network.LeaderboardEntry
import com.athreya.mathworkout.data.network.ScoreUtils
import com.athreya.mathworkout.viewmodel.GlobalScoreViewModel
import com.athreya.mathworkout.viewmodel.GlobalScoreUiState

/**
 * Global Score screen showing weekly leaderboard and user stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalScoreScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = remember { GlobalScoreViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Show success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            kotlinx.coroutines.delay(5000)
            viewModel.clearErrorMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Global Scores") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // User Status Card
                item {
                    UserStatusCard(
                        uiState = uiState,
                        onRegisterClick = { viewModel.showRegistrationDialog() }
                    )
                }
                
                // Messages
                if (uiState.successMessage != null) {
                    item {
                        SuccessMessage(uiState.successMessage!!)
                    }
                }
                
                if (uiState.errorMessage != null) {
                    item {
                        ErrorMessage(uiState.errorMessage!!)
                    }
                }
                
                // Game Mode Filter
                item {
                    GameModeFilter(
                        selectedMode = uiState.selectedGameMode,
                        availableModes = uiState.availableGameModes,
                        onModeSelected = { viewModel.selectGameMode(it) },
                        getDisplayName = { viewModel.getGameModeDisplayName(it) }
                    )
                }
                
                // Weekly Leaderboard
                item {
                    WeeklyLeaderboardCard(
                        leaderboard = uiState.weeklyLeaderboard,
                        isLoading = uiState.isLoading,
                        currentUserRank = viewModel.getCurrentUserRank()
                    )
                }
                
                // User Personal Scores (if registered)
                if (uiState.isUserRegistered && uiState.userScores.isNotEmpty()) {
                    item {
                        PersonalScoresCard(userScores = uiState.userScores)
                    }
                }
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading...")
                        }
                    }
                }
            }
        }
    }
    
    // Registration Dialog
    if (uiState.showRegistrationDialog) {
        RegistrationDialog(
            onRegister = { userName ->
                viewModel.registerUser(userName)
            },
            onDismiss = { viewModel.hideRegistrationDialog() }
        )
    }
}

@Composable
private fun UserStatusCard(
    uiState: GlobalScoreUiState,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.isUserRegistered) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isUserRegistered) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Welcome, ${uiState.userName}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your scores are being tracked globally",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Join Global Competition",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Register to compete with players worldwide",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRegisterClick) {
                    Text("Register Now")
                }
            }
        }
    }
}

@Composable
private fun GameModeFilter(
    selectedMode: String,
    availableModes: List<String>,
    onModeSelected: (String) -> Unit,
    getDisplayName: (String) -> String
) {
    Column {
        Text(
            text = "Filter by Game Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableModes) { mode ->
                FilterChip(
                    onClick = { onModeSelected(mode) },
                    label = { Text(getDisplayName(mode)) },
                    selected = selectedMode == mode
                )
            }
        }
    }
}

@Composable
private fun WeeklyLeaderboardCard(
    leaderboard: com.athreya.mathworkout.data.network.WeeklyLeaderboard?,
    isLoading: Boolean,
    currentUserRank: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 This Week's Top 10",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (currentUserRank != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Your Rank: #$currentUserRank",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (leaderboard != null) {
                Text(
                    text = "${leaderboard.weekStartDate} - ${leaderboard.weekEndDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "${leaderboard.totalParticipants} total participants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (leaderboard?.entries?.isEmpty() == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No scores this week yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Be the first to submit a score!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                leaderboard?.entries?.forEachIndexed { index, entry ->
                    LeaderboardEntryItem(
                        entry = entry,
                        rank = index + 1
                    )
                    if (index < leaderboard.entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardEntryItem(
    entry: LeaderboardEntry,
    rank: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (entry.isCurrentUser) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(8.dp)
                } else {
                    Modifier.padding(8.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank with medal icons for top 3
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (rank) {
                1 -> Text("🥇", fontSize = 20.sp)
                2 -> Text("🥈", fontSize = 20.sp)
                3 -> Text("🥉", fontSize = 20.sp)
                else -> Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // User info and game details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.userName + if (entry.isCurrentUser) " (You)" else "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal
            )
            Row {
                Text(
                    text = getGameModeDisplayName(entry.gameMode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.difficulty != null) {
                    Text(
                        text = " • ${entry.difficulty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Score and time
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.score} pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = ScoreUtils.formatTime(entry.timeInMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PersonalScoresCard(
    userScores: List<com.athreya.mathworkout.data.network.GlobalScore>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Your Personal Bests",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            userScores.take(5).forEach { score ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = getGameModeDisplayName(score.gameMode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (score.difficulty != null) {
                            Text(
                                text = score.difficulty,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${score.score} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ScoreUtils.formatTime(score.timeInMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun RegistrationDialog(
    onRegister: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Global Competition",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Enter your display name to compete with players worldwide:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (userName.isNotBlank()) {
                                onRegister(userName.trim())
                            }
                        },
                        enabled = userName.isNotBlank()
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

private fun getGameModeDisplayName(gameMode: String): String {
    return when (gameMode) {
        "ADDITION_SUBTRACTION" -> "Addition & Subtraction"
        "MULTIPLICATION_DIVISION" -> "Multiplication & Division"
        "TEST_ME" -> "Test Me"
        "BRAIN_TEASER" -> "Brain Teaser"
        "SUDOKU" -> "Sudoku"
        else -> gameMode
    }
}