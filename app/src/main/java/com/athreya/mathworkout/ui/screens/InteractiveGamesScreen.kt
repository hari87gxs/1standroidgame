package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.GameDifficulty
import com.athreya.mathworkout.data.GameType


/**
 * Interactive Games Screen - Hub for math games, puzzles, and riddles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveGamesScreen(
    onBackClick: () -> Unit,
    onDailyRiddleClick: () -> Unit,
    onGameClick: (gameType: String, difficulty: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.EASY) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Games") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "🎮 Math Games & Puzzles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Challenge yourself with:\n• Number Puzzles 🧩\n• Pattern Matching 🔢\n• Story Problems 📖\n• Daily Riddles 🤔",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Difficulty selector
            item {
                Text(
                    text = "Choose Difficulty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameDifficulty.values().forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty },
                            label = { 
                                Text(
                                    text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (selectedDifficulty == difficulty) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Game type buttons
            item {
                Text(
                    text = "Select Game Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(GameType.values().filter { it != GameType.MATH_RIDDLE && it != GameType.NUMBER_SEQUENCE }) { gameType ->
                GameTypeCard(
                    gameType = gameType,
                    difficulty = selectedDifficulty,
                    onClick = {
                        onGameClick(
                            gameType.name,
                            selectedDifficulty.name
                        )
                    }
                )
            }
            
            // Daily Riddle
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Special Challenge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                DailyRiddleCard(
                    onClick = onDailyRiddleClick
                )
            }
        }
    }
}

@Composable
private fun GameTypeCard(
    gameType: GameType,
    difficulty: GameDifficulty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, title, description) = when (gameType) {
        GameType.NUMBER_PUZZLE -> Triple("🧩", "Number Puzzles", "Find missing numbers in patterns")
        GameType.PATTERN_MATCHING -> Triple("🔢", "Pattern Matching", "Identify and predict patterns")
        GameType.STORY_PROBLEM -> Triple("📖", "Story Problems", "Solve real-world math problems")
        else -> Triple("🎮", "Math Game", "Fun math challenge")
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DailyRiddleCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤔", fontSize = 28.sp)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Math Riddle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Challenge your brain with today's riddle!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
