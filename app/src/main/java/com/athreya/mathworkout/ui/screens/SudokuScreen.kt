package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.game.SudokuCell
import com.athreya.mathworkout.game.SudokuDifficulty
import com.athreya.mathworkout.viewmodel.SudokuViewModel
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    onBackPressed: () -> Unit,
    sudokuViewModel: SudokuViewModel = viewModel()
) {
    val gameState by sudokuViewModel.gameState.collectAsState()
    val elapsedTime by sudokuViewModel.elapsedTime.collectAsState()
    val isGameActive by sudokuViewModel.isGameActive.collectAsState()
    val gameStats by remember(gameState, elapsedTime) { 
        derivedStateOf { sudokuViewModel.getGameStats() }
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
        sudokuViewModel.startNewGame(SudokuDifficulty.EASY)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "⚠️ Invalid state - check for conflicts",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Number selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Select Number",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..9).toList()) { number ->
                        NumberButton(
                            number = number,
                            onClick = { sudokuViewModel.makeMove(number) },
                            enabled = isGameActive
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // New game button
        Button(
            onClick = { sudokuViewModel.startNewGame(SudokuDifficulty.EASY) },
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
    Column(modifier = modifier) {
        for (row in 0..8) {
            Row {
                for (col in 0..8) {
                    val cell = gameState.grid[row][col]
                    SudokuCellView(
                        cell = cell,
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            .let { mod ->
                                // Add thicker borders for 3x3 box separation
                                var newMod = mod
                                if (row % 3 == 0 && row > 0) {
                                    newMod = newMod.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (col % 3 == 0 && col > 0) {
                                    newMod = newMod.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                newMod
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = when {
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
                color = if (cell.isGiven) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        } else if (cell.notes.isNotEmpty()) {
            // Show notes as small numbers
            Column {
                for (row in 0..2) {
                    Row {
                        for (col in 1..3) {
                            val number = row * 3 + col
                            Text(
                                text = if (number in cell.notes) number.toString() else " ",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(8.dp),
                                textAlign = TextAlign.Center
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
        modifier = modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number.toString(),
            fontSize = 16.sp,
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