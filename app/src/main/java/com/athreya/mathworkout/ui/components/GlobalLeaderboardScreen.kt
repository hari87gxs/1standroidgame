package com.athreya.mathworkout.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.HighScore
import com.athreya.mathworkout.viewmodel.GlobalLeaderboardUiState
import com.athreya.mathworkout.viewmodel.GlobalLeaderboardViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Global Leaderboard Screen that displays worldwide rankings
 * Features:
 * - Global leaderboard with player rankings
 * - Filter by game mode and difficulty  
 * - Sync status and manual sync option
 * - Player's current global rank
 * - Offline/online status indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalLeaderboardScreen(
    viewModel: GlobalLeaderboardViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    
    // Check online status when screen loads
    LaunchedEffect(Unit) {
        viewModel.checkOnlineStatus()
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Global Leaderboard") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Sync Button
                IconButton(
                    onClick = { viewModel.syncLocalScores() },
                    enabled = !uiState.isSyncing && uiState.isOnline
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = "Sync Scores")
                    }
                }
                
                // Refresh Button
                IconButton(
                    onClick = { viewModel.refreshLeaderboard() },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        // Status Indicators
        StatusIndicators(
            uiState = uiState,
            syncStatus = syncStatus,
            onDismissError = { viewModel.clearError() },
            onDismissSyncMessage = { viewModel.clearSyncMessage() }
        )
        
        // Filter Section
        FilterSection(
            selectedGameMode = uiState.selectedGameMode,
            selectedDifficulty = uiState.selectedDifficulty,
            onGameModeSelected = { viewModel.filterByGameMode(it) },
            onDifficultySelected = { viewModel.filterByDifficulty(it) }
        )
        
        // Player Rank Section
        uiState.playerRank?.let { rank ->
            PlayerRankCard(
                rank = rank,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Leaderboard Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.align(Alignment.Center))
                }
                uiState.globalScores.isEmpty() && uiState.error == null -> {
                    EmptyState(
                        isOnline = uiState.isOnline,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LeaderboardList(
                        scores = uiState.globalScores,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicators(
    uiState: GlobalLeaderboardUiState,
    syncStatus: com.athreya.mathworkout.data.SyncStatus?,
    onDismissError: () -> Unit,
    onDismissSyncMessage: () -> Unit
) {
    Column {
        // Online/Offline Status
        Surface(
            color = if (uiState.isOnline) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (uiState.isOnline) Color.Green else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isOnline) "Online" else "Offline - Showing cached data",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.isOnline) Color.Green else Color.Red
                )
                
                // Sync Status
                syncStatus?.let { status ->
                    if (status.pendingSyncs > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${status.pendingSyncs} pending sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
        
        // Error Message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = onDismissError) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
        
        // Success Message
        uiState.lastSyncMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = onDismissSyncMessage) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    selectedGameMode: GameMode?,
    selectedDifficulty: Difficulty?,
    onGameModeSelected: (GameMode?) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit
) {
    var gameModeExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Mode Filter
        ExposedDropdownMenuBox(
            expanded = gameModeExpanded,
            onExpandedChange = { gameModeExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedGameMode?.name ?: "All Modes",
                onValueChange = { },
                readOnly = true,
                label = { Text("Game Mode") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gameModeExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = gameModeExpanded,
                onDismissRequest = { gameModeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Modes") },
                    onClick = {
                        onGameModeSelected(null)
                        gameModeExpanded = false
                    }
                )
                GameMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.name.replace("_", " ")) },
                        onClick = {
                            onGameModeSelected(mode)
                            gameModeExpanded = false
                        }
                    )
                }
            }
        }
        
        // Difficulty Filter
        ExposedDropdownMenuBox(
            expanded = difficultyExpanded,
            onExpandedChange = { difficultyExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedDifficulty?.name ?: "All Levels",
                onValueChange = { },
                readOnly = true,
                label = { Text("Difficulty") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = { difficultyExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Levels") },
                    onClick = {
                        onDifficultySelected(null)
                        difficultyExpanded = false
                    }
                )
                Difficulty.values().forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.name) },
                        onClick = {
                            onDifficultySelected(difficulty)
                            difficultyExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerRankCard(
    rank: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Your Global Rank: #$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading global leaderboard...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EmptyState(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isOnline) Icons.Default.Leaderboard else Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isOnline) {
                "No scores found\nBe the first to set a global record!"
            } else {
                "No cached data available\nConnect to internet to view global scores"
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun LeaderboardList(
    scores: List<HighScore>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(scores) { index, score ->
            LeaderboardItem(
                rank = index + 1,
                score = score
            )
        }
    }
}

@Composable
private fun LeaderboardItem(
    rank: Int,
    score: HighScore
) {
    // Load badges for this player (in a real app, this would come from the score data)
    val context = androidx.compose.ui.platform.LocalContext.current
    val badgeManager = remember { com.athreya.mathworkout.data.BadgeManager(com.athreya.mathworkout.data.UserPreferencesManager(context)) }
    val unlockedBadges = remember { badgeManager.getUnlockedBadges().take(3) } // Show top 3 badges
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .widthIn(min = 48.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver  
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "#$rank",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else Color.White
                    )
                }
            }
            
            // Player Info and Score Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = score.playerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Display badges next to player name
                    if (unlockedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        com.athreya.mathworkout.ui.components.BadgeRow(
                            badges = unlockedBadges,
                            maxBadges = 3
                        )
                    }
                }
                Text(
                    text = "${score.gameMode.replace("_", " ")} • ${score.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = formatTimestamp(score.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Score and Stats
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${score.finalScore} pts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatTime(score.timeTaken),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (score.wrongAttempts > 0) {
                    Text(
                        text = "${score.wrongAttempts} wrong",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) {
        String.format("%d:%02d", minutes, seconds)
    } else {
        String.format("%ds", seconds)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}