package com.athreya.mathworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.game.SudokuDifficulty
import com.athreya.mathworkout.game.SudokuEngine
import com.athreya.mathworkout.game.SudokuGameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for managing Sudoku game state and logic.
 */
class SudokuViewModel : ViewModel() {
    
    private val sudokuEngine = SudokuEngine()
    
    private val _gameState = MutableStateFlow(SudokuGameState())
    val gameState: StateFlow<SudokuGameState> = _gameState.asStateFlow()
    
    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime: StateFlow<Duration> = _elapsedTime.asStateFlow()
    
    private val _isGameActive = MutableStateFlow(false)
    val isGameActive: StateFlow<Boolean> = _isGameActive.asStateFlow()
    
    private var gameStartTime: Long = 0
    
    /**
     * Start a new Sudoku game with the specified difficulty.
     */
    fun startNewGame(difficulty: SudokuDifficulty = SudokuDifficulty.EASY) {
        viewModelScope.launch {
            val newGameState = sudokuEngine.generatePuzzle(difficulty)
            _gameState.value = newGameState
            _isGameActive.value = true
            _elapsedTime.value = Duration.ZERO
            gameStartTime = System.currentTimeMillis()
        }
    }
    
    /**
     * Select a cell in the Sudoku grid.
     */
    fun selectCell(row: Int, col: Int) {
        if (!_isGameActive.value) return
        
        val newGameState = sudokuEngine.selectCell(_gameState.value, row, col)
        _gameState.value = newGameState
    }
    
    /**
     * Make a move by placing a number in the selected cell.
     */
    fun makeMove(number: Int) {
        if (!_isGameActive.value) return
        
        val currentState = _gameState.value
        if (currentState.selectedRow == -1 || currentState.selectedCol == -1) {
            return
        }
        
        val newGameState = sudokuEngine.makeMove(
            currentState,
            currentState.selectedRow,
            currentState.selectedCol,
            number
        )
        
        _gameState.value = newGameState
        
        // Check if game is completed
        if (newGameState.isCompleted) {
            _isGameActive.value = false
            updateElapsedTime() // Final time update
        }
    }
    
    /**
     * Toggle note mode on/off.
     */
    fun toggleNoteMode() {
        if (!_isGameActive.value) return
        
        val newGameState = sudokuEngine.toggleNoteMode(_gameState.value)
        _gameState.value = newGameState
    }
    
    /**
     * Undo the last move.
     */
    fun undoMove() {
        if (!_isGameActive.value) return
        
        val newGameState = sudokuEngine.undoMove(_gameState.value)
        _gameState.value = newGameState
    }
    
    /**
     * Erase the content of the selected cell.
     */
    fun eraseCell() {
        if (!_isGameActive.value) return
        
        val newGameState = sudokuEngine.eraseCell(_gameState.value)
        _gameState.value = newGameState
    }
    
    /**
     * Get a hint for the selected cell.
     */
    fun getHint() {
        if (!_isGameActive.value) return
        
        val newGameState = sudokuEngine.getHint(_gameState.value)
        _gameState.value = newGameState
    }
    
    /**
     * Update the elapsed time.
     */
    fun updateElapsedTime() {
        if (_isGameActive.value) {
            val currentTime = System.currentTimeMillis()
            _elapsedTime.value = (currentTime - gameStartTime).seconds
        }
    }
    
    /**
     * Pause the game.
     */
    fun pauseGame() {
        _isGameActive.value = false
        updateElapsedTime()
    }
    
    /**
     * Resume the game.
     */
    fun resumeGame() {
        if (!_gameState.value.isCompleted) {
            _isGameActive.value = true
            gameStartTime = System.currentTimeMillis() - _elapsedTime.value.inWholeMilliseconds
        }
    }
    
    /**
     * Reset the game state.
     */
    fun resetGame() {
        _gameState.value = SudokuGameState()
        _elapsedTime.value = Duration.ZERO
        _isGameActive.value = false
        gameStartTime = 0
    }
    
    /**
     * Get the current game statistics.
     */
    fun getGameStats(): SudokuGameStats {
        val currentState = _gameState.value
        val filledCells = currentState.grid.sumOf { row -> 
            row.count { cell -> cell.value > 0 } 
        }
        val totalCells = 81
        val givenCells = currentState.grid.sumOf { row -> 
            row.count { cell -> cell.isGiven } 
        }
        
        return SudokuGameStats(
            filledCells = filledCells,
            totalCells = totalCells,
            givenCells = givenCells,
            progressPercentage = ((filledCells.toFloat() / totalCells) * 100).toInt(),
            elapsedTime = _elapsedTime.value,
            isCompleted = currentState.isCompleted,
            isValid = currentState.isValid,
            movesCount = currentState.moveHistory.size
        )
    }
}

/**
 * Data class representing Sudoku game statistics.
 */
data class SudokuGameStats(
    val filledCells: Int,
    val totalCells: Int,
    val givenCells: Int,
    val progressPercentage: Int,
    val elapsedTime: Duration,
    val isCompleted: Boolean,
    val isValid: Boolean,
    val movesCount: Int
)