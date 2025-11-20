package com.athreya.mathworkout.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.SettingsManager
import com.athreya.mathworkout.game.SudokuCell
import com.athreya.mathworkout.game.SudokuDifficulty
import com.athreya.mathworkout.viewmodel.GlobalScoreViewModel
import com.athreya.mathworkout.viewmodel.SudokuViewModel
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    onBackPressed: () -> Unit,
    isDailyChallenge: Boolean = false,
    onChallengeComplete: ((timeTaken: Long, wrongAttempts: Int) -> Unit)? = null,
    globalScoreViewModel: GlobalScoreViewModel,
    sudokuViewModel: SudokuViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }
    val settings by settingsManager.gameSettings.collectAsState(
        initial = com.athreya.mathworkout.data.GameSettings(
            difficulty = com.athreya.mathworkout.data.Difficulty.EASY,
            questionCount = 10
        )
    )
    val gameState by sudokuViewModel.gameState.collectAsState()
    val elapsedTime by sudokuViewModel.elapsedTime.collectAsState()
    val isGameActive by sudokuViewModel.isGameActive.collectAsState()
    val gameStats by remember(gameState, elapsedTime) { 
        derivedStateOf { sudokuViewModel.getGameStats() }
    }
    
    // Track if we've already saved the completion
    var hasCompletedChallenge by remember { mutableStateOf(false) }
    
    // Handle completion for daily challenge and score saving
    LaunchedEffect(gameState.isCompleted) {
        if (gameState.isCompleted && !hasCompletedChallenge) {
            hasCompletedChallenge = true
            
            // Save to global leaderboard
            scope.launch {
                try {
                    val timeInMillis = elapsedTime.inWholeMilliseconds
                    
                    globalScoreViewModel.submitScore(
                        gameMode = GameMode.SUDOKU,
                        difficulty = gameStats.difficulty.name,
                        wrongAttempts = 0, // Sudoku doesn't track wrong attempts
                        timeInMillis = timeInMillis
                    )
                } catch (e: Exception) {
                    // Handle error silently or show toast
                    e.printStackTrace()
                }
            }
            
            // Complete daily challenge if applicable
            if (isDailyChallenge) {
                val timeInMillis = elapsedTime.inWholeMilliseconds
                onChallengeComplete?.invoke(timeInMillis, 0)
            }
        }
    }
    
    // Update timer periodically
    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            while (isGameActive) {
                kotlinx.coroutines.delay(1000)
                sudokuViewModel.updateElapsedTime()
            }
        }
    }
    
    // Auto-start a new game when screen loads
    LaunchedEffect(Unit) {
        // Convert game difficulty to Sudoku difficulty
        val sudokuDiff = when (settings.difficulty) {
            com.athreya.mathworkout.data.Difficulty.EASY -> SudokuDifficulty.EASY
            com.athreya.mathworkout.data.Difficulty.MEDIUM -> SudokuDifficulty.MEDIUM
            com.athreya.mathworkout.data.Difficulty.COMPLEX -> SudokuDifficulty.HARD
        }
        sudokuViewModel.startNewGame(sudokuDiff)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header with back button and timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackPressed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Back")
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sudoku",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Progress indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${gameStats.progressPercentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = gameStats.progressPercentage / 100f,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Game status
        if (gameState.isCompleted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "🎉 Puzzle Completed! Time: ${formatTime(elapsedTime)}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else if (!gameState.isValid) {
            // Conflicts are now shown visually in the grid with red highlighting
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sudoku Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            SudokuGrid(
                gameState = gameState,
                onCellClick = { row, col -> 
                    sudokuViewModel.selectCell(row, col)
                },
                modifier = Modifier.padding(8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlButton(
                text = "Undo",
                onClick = { sudokuViewModel.undoMove() },
                enabled = gameState.moveHistory.isNotEmpty() && isGameActive
            )
            
            ControlButton(
                text = "Erase", 
                onClick = { sudokuViewModel.eraseCell() },
                enabled = gameState.selectedRow != -1 && isGameActive
            )
            
            ControlButton(
                text = if (gameState.noteMode) "Notes ON" else "Notes OFF",
                onClick = { sudokuViewModel.toggleNoteMode() },
                enabled = isGameActive,
                isSelected = gameState.noteMode
            )
            
            ControlButton(
                text = "Hint",
                onClick = { sudokuViewModel.getHint() },
                enabled = gameState.selectedRow != -1 && isGameActive
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Number selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Select Number",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..9).forEach { number ->
                        NumberButton(
                            number = number,
                            onClick = { sudokuViewModel.makeMove(number) },
                            enabled = isGameActive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // New game button
        Button(
            onClick = { 
                // Convert game difficulty to Sudoku difficulty
                val sudokuDiff = when (settings.difficulty) {
                    com.athreya.mathworkout.data.Difficulty.EASY -> SudokuDifficulty.EASY
                    com.athreya.mathworkout.data.Difficulty.MEDIUM -> SudokuDifficulty.MEDIUM
                    com.athreya.mathworkout.data.Difficulty.COMPLEX -> SudokuDifficulty.HARD
                }
                sudokuViewModel.startNewGame(sudokuDiff)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "New Game",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SudokuGrid(
    gameState: com.athreya.mathworkout.game.SudokuGameState,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate all conflicting cells across the entire grid
    val allConflicts = remember(gameState) {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        for (r in 0..8) {
            for (c in 0..8) {
                val cellConflicts = findConflictingCells(gameState.grid, r, c)
                if (cellConflicts.isNotEmpty()) {
                    // Add the cell itself if it has conflicts
                    conflicts.add(Pair(r, c))
                    // Add all cells it conflicts with
                    conflicts.addAll(cellConflicts)
                }
            }
        }
        conflicts
    }
    
    Box(
        modifier = modifier
            .background(Color.Black) // Black background for grid lines
    ) {
        Column {
            for (row in 0..8) {
                Row {
                    for (col in 0..8) {
                        val cell = gameState.grid[row][col]
                        val hasConflict = Pair(row, col) in allConflicts
                        
                        // Calculate border widths - thick for 3x3 boundaries
                        val topBorder = if (row % 3 == 0) 3.dp else 1.dp
                        val leftBorder = if (col % 3 == 0) 3.dp else 1.dp
                        val rightBorder = if (col == 8) 3.dp else 0.dp
                        val bottomBorder = if (row == 8) 3.dp else 0.dp
                        
                        SudokuCellView(
                            cell = cell,
                            onClick = { onCellClick(row, col) },
                            hasConflict = hasConflict,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(
                                    start = leftBorder,
                                    top = topBorder,
                                    end = rightBorder,
                                    bottom = bottomBorder
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Find cells that conflict with the given cell at (row, col)
 * Returns a set of Pair<row, col> representing conflicting cells
 */
private fun findConflictingCells(grid: List<List<SudokuCell>>, row: Int, col: Int): Set<Pair<Int, Int>> {
    val conflicts = mutableSetOf<Pair<Int, Int>>()
    val currentCell = grid[row][col]
    
    if (currentCell.value == 0) return emptySet()
    
    val value = currentCell.value
    
    // Check row for conflicts
    for (c in 0..8) {
        if (c != col && grid[row][c].value == value) {
            conflicts.add(Pair(row, c))
        }
    }
    
    // Check column for conflicts
    for (r in 0..8) {
        if (r != row && grid[r][col].value == value) {
            conflicts.add(Pair(r, col))
        }
    }
    
    // Check 3x3 box for conflicts
    val boxRow = row / 3
    val boxCol = col / 3
    for (r in boxRow * 3..(boxRow * 3 + 2)) {
        for (c in boxCol * 3..(boxCol * 3 + 2)) {
            if ((r != row || c != col) && grid[r][c].value == value) {
                conflicts.add(Pair(r, c))
            }
        }
    }
    
    return conflicts
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    onClick: () -> Unit,
    hasConflict: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    hasConflict -> Color(0xFFFFCDD2) // Light red for conflicts
                    cell.isSelected -> MaterialTheme.colorScheme.primaryContainer
                    cell.isGiven -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value > 0) {
            Text(
                text = cell.value.toString(),
                fontSize = 18.sp,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    hasConflict -> Color(0xFFB71C1C) // Dark red text for conflicts
                    cell.isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    cell.isGiven -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        } else if (cell.notes.isNotEmpty()) {
            // Dynamic font sizing based on number of notes
            val noteCount = cell.notes.size
            val fontSize = when {
                noteCount <= 2 -> 13.sp
                noteCount <= 4 -> 10.sp
                noteCount <= 6 -> 8.sp
                else -> 6.sp
            }
            
            // Show notes as small numbers flowing from left to right, top to bottom
            val sortedNotes = cell.notes.sorted()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // Split into rows of max 3 numbers each
                sortedNotes.chunked(3).forEach { rowNumbers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        rowNumbers.forEach { number ->
                            Text(
                                text = number.toString() + " ",
                                fontSize = fontSize,
                                color = if (cell.isSelected) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal,
                                lineHeight = fontSize
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NumberButton(
    number: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = number.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
/**
 * Calculate score for Sudoku based on time and difficulty
 * Faster completion = higher score
 * Harder difficulty = higher base score
 */
private fun calculateSudokuScore(timeInSeconds: Int, difficulty: SudokuDifficulty): Int {
    // Base score by difficulty
    val baseScore = when (difficulty) {
        SudokuDifficulty.EASY -> 100
        SudokuDifficulty.MEDIUM -> 200
        SudokuDifficulty.HARD -> 300
    }
    
    // Time bonus: reduce score based on time taken
    // Target times: Easy=5min, Medium=10min, Hard=15min
    val targetTime = when (difficulty) {
        SudokuDifficulty.EASY -> 300    // 5 minutes
        SudokuDifficulty.MEDIUM -> 600  // 10 minutes
        SudokuDifficulty.HARD -> 900    // 15 minutes
    }
    
    // Calculate time multiplier (1.0 at target time, 2.0 if completed in half the time)
    val timeMultiplier = if (timeInSeconds > 0) {
        (targetTime.toFloat() / timeInSeconds.toFloat()).coerceIn(0.5f, 2.0f)
    } else {
        1.0f
    }
    
    return (baseScore * timeMultiplier).toInt()
}
