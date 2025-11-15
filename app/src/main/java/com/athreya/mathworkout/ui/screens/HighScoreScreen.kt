package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.data.HighScore
import com.athreya.mathworkout.viewmodel.HighScoreFilter
import com.athreya.mathworkout.viewmodel.HighScoreViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * HighScoreScreen Composable - Displays all high scores with filtering options.
 * 
 * This screen demonstrates:
 * - LazyColumn for efficient list display
 * - Dropdown menus for filtering
 * - Date formatting
 * - Empty states
 * 
 * @param onBackClick Callback called when back button is pressed
 * @param viewModel ViewModel that manages high score data and filtering
 * @param modifier Optional modifier for customizing appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighScoreScreen(
    onBackClick: () -> Unit,
    viewModel: HighScoreViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // Observe the high score state
    val uiState by viewModel.uiState.collectAsState()
    
    // State for filter dropdown
    var showFilterMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("High Scores") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Filter button
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                        
                        // Filter dropdown menu
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            // All scores option
                            DropdownMenuItem(
                                text = { Text("All Scores") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.ALL)
                                    showFilterMenu = false
                                }
                            )
                            
                            HorizontalDivider()
                            
                            // Game mode filters
                            Text(
                                text = "By Game Mode",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Addition & Subtraction") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.ADDITION_SUBTRACTION)
                                    showFilterMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Multiplication & Division") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.MULTIPLICATION_DIVISION)
                                    showFilterMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Test Me") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.TEST_ME)
                                    showFilterMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Brain Teaser") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.BRAIN_TEASER)
                                    showFilterMenu = false
                                }
                            )
                            
                            HorizontalDivider()
                            
                            // Difficulty filters
                            Text(
                                text = "By Difficulty",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Easy") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.EASY)
                                    showFilterMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Medium") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.MEDIUM)
                                    showFilterMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Complex") },
                                onClick = {
                                    viewModel.setFilter(HighScoreFilter.COMPLEX)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.highScores.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No scores yet!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Play some games to see your scores here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            // High scores list
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current filter indicator
                item {
                    if (uiState.selectedFilter != HighScoreFilter.ALL) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "Filtered by: ${getFilterDisplayName(uiState.selectedFilter)}",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // High score items
                items(uiState.highScores) { score ->
                    HighScoreItem(
                        highScore = score,
                        rank = uiState.highScores.indexOf(score) + 1,
                        formatTime = viewModel::formatTime
                    )
                }
            }
        }
    }
}

/**
 * Composable for displaying a single high score item.
 * 
 * @param highScore The high score data to display
 * @param rank The ranking position (1st, 2nd, etc.)
 * @param formatTime Function to format time values
 */
@Composable
private fun HighScoreItem(
    highScore: HighScore,
    rank: Int,
    formatTime: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank and trophy for top 3
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (rank) {
                        1 -> "🏆"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#$rank"
                    },
                    fontSize = 18.sp,
                    modifier = Modifier.width(40.dp)
                )
                
                Column {
                    // Game mode and difficulty
                    Text(
                        text = getDisplayGameMode(highScore.gameMode),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${highScore.difficulty.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Score and details
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${highScore.finalScore} pts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatTime(highScore.timeTaken),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (highScore.wrongAttempts > 0) {
                    Text(
                        text = "${highScore.wrongAttempts} wrong",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (highScore.bonusMultiplier > 1.0f) {
                    Text(
                        text = "${highScore.bonusMultiplier}x bonus",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get display name for game modes.
 */
private fun getDisplayGameMode(gameMode: String): String {
    return when (gameMode) {
        "ADDITION_SUBTRACTION" -> "Add & Subtract"
        "MULTIPLICATION_DIVISION" -> "Multiply & Divide"
        "TEST_ME" -> "Test Me"
        "BRAIN_TEASER" -> "Brain Teaser"
        else -> gameMode
    }
}

/**
 * Helper function to get display name for filters.
 */
private fun getFilterDisplayName(filter: HighScoreFilter): String {
    return when (filter) {
        HighScoreFilter.ALL -> "All"
        HighScoreFilter.ADDITION_SUBTRACTION -> "Addition & Subtraction"
        HighScoreFilter.MULTIPLICATION_DIVISION -> "Multiplication & Division"
        HighScoreFilter.TEST_ME -> "Test Me"
        HighScoreFilter.BRAIN_TEASER -> "Brain Teaser"
        HighScoreFilter.EASY -> "Easy"
        HighScoreFilter.MEDIUM -> "Medium"
        HighScoreFilter.COMPLEX -> "Complex"
    }
}

/**
 * Helper function to format timestamp as a readable date.
 */
private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Preview for the High Score screen.
 */
@Preview(showBackground = true)
@Composable
private fun HighScoreScreenPreview() {
    MaterialTheme {
        // Note: In a real preview, we'd need to provide mock data
    }
}