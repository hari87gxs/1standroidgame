package com.athreya.mathworkout.game

/**
 * Represents a single cell in the Sudoku grid.
 * 
 * @param value The number in the cell (1-9), or 0 if empty
 * @param isGiven Whether this cell was pre-filled (cannot be modified)
 * @param notes Set of possible numbers for this cell (for note-taking)
 * @param isSelected Whether this cell is currently selected
 */
data class SudokuCell(
    val value: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isSelected: Boolean = false
) {
    val isEmpty: Boolean get() = value == 0
    val isValid: Boolean get() = value in 0..9
}

/**
 * Represents the complete Sudoku game state.
 * 
 * @param grid 9x9 grid of SudokuCell objects
 * @param selectedRow Currently selected row (-1 if none)
 * @param selectedCol Currently selected column (-1 if none)
 * @param isCompleted Whether the puzzle is solved
 * @param isValid Whether the current state is valid (no conflicts)
 * @param noteMode Whether we're in note-taking mode
 */
data class SudokuGameState(
    val grid: List<List<SudokuCell>> = List(9) { List(9) { SudokuCell() } },
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isCompleted: Boolean = false,
    val isValid: Boolean = true,
    val noteMode: Boolean = false,
    val moveHistory: List<SudokuMove> = emptyList()
)

/**
 * Represents a move in the Sudoku game for undo functionality.
 */
data class SudokuMove(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val previousNotes: Set<Int>,
    val newValue: Int,
    val newNotes: Set<Int>
)

/**
 * Sudoku game engine that handles all game logic.
 */
class SudokuEngine {
    
    /**
     * Generate a new Sudoku puzzle with the specified difficulty.
     * 
     * @param difficulty The difficulty level (affects number of pre-filled cells)
     * @return A new SudokuGameState with a valid puzzle
     */
    fun generatePuzzle(difficulty: SudokuDifficulty = SudokuDifficulty.EASY): SudokuGameState {
        // Start with a complete, valid Sudoku solution
        val completedGrid = generateCompleteSolution()
        
        // Remove numbers based on difficulty
        val cellsToRemove = when (difficulty) {
            SudokuDifficulty.EASY -> 35    // ~35 empty cells
            SudokuDifficulty.MEDIUM -> 45  // ~45 empty cells
            SudokuDifficulty.HARD -> 55    // ~55 empty cells
        }
        
        val puzzleGrid = removeNumbers(completedGrid, cellsToRemove)
        
        return SudokuGameState(
            grid = puzzleGrid,
            isValid = true,
            isCompleted = false
        )
    }
    
    /**
     * Make a move on the Sudoku grid.
     */
    fun makeMove(
        gameState: SudokuGameState,
        row: Int,
        col: Int,
        number: Int
    ): SudokuGameState {
        if (row !in 0..8 || col !in 0..8 || number !in 0..9) {
            return gameState
        }
        
        val currentCell = gameState.grid[row][col]
        if (currentCell.isGiven) {
            return gameState // Cannot modify given cells
        }
        
        // Create new grid with updated cell
        val newGrid = gameState.grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) {
                    if (gameState.noteMode && number > 0) {
                        // Toggle note
                        val newNotes = if (cell.notes.contains(number)) {
                            cell.notes - number
                        } else {
                            cell.notes + number
                        }
                        cell.copy(notes = newNotes, value = 0)
                    } else {
                        // Set value and clear notes
                        cell.copy(value = number, notes = emptySet())
                    }
                } else {
                    cell
                }
            }
        }
        
        // Create move for undo history
        val move = SudokuMove(
            row = row,
            col = col,
            previousValue = currentCell.value,
            previousNotes = currentCell.notes,
            newValue = if (gameState.noteMode) currentCell.value else number,
            newNotes = if (gameState.noteMode && number > 0) {
                if (currentCell.notes.contains(number)) {
                    currentCell.notes - number
                } else {
                    currentCell.notes + number
                }
            } else {
                emptySet()
            }
        )
        
        return gameState.copy(
            grid = newGrid,
            isValid = isValidState(newGrid),
            isCompleted = isCompleted(newGrid),
            moveHistory = gameState.moveHistory + move
        )
    }
    
    /**
     * Select a cell in the grid.
     */
    fun selectCell(gameState: SudokuGameState, row: Int, col: Int): SudokuGameState {
        if (row !in 0..8 || col !in 0..8) {
            return gameState.copy(selectedRow = -1, selectedCol = -1)
        }
        
        val newGrid = gameState.grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                cell.copy(isSelected = r == row && c == col)
            }
        }
        
        return gameState.copy(
            grid = newGrid,
            selectedRow = row,
            selectedCol = col
        )
    }
    
    /**
     * Toggle note mode on/off.
     */
    fun toggleNoteMode(gameState: SudokuGameState): SudokuGameState {
        return gameState.copy(noteMode = !gameState.noteMode)
    }
    
    /**
     * Undo the last move.
     */
    fun undoMove(gameState: SudokuGameState): SudokuGameState {
        if (gameState.moveHistory.isEmpty()) {
            return gameState
        }
        
        val lastMove = gameState.moveHistory.last()
        val newGrid = gameState.grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == lastMove.row && c == lastMove.col) {
                    cell.copy(
                        value = lastMove.previousValue,
                        notes = lastMove.previousNotes
                    )
                } else {
                    cell
                }
            }
        }
        
        return gameState.copy(
            grid = newGrid,
            isValid = isValidState(newGrid),
            isCompleted = isCompleted(newGrid),
            moveHistory = gameState.moveHistory.dropLast(1)
        )
    }
    
    /**
     * Erase the selected cell.
     */
    fun eraseCell(gameState: SudokuGameState): SudokuGameState {
        if (gameState.selectedRow == -1 || gameState.selectedCol == -1) {
            return gameState
        }
        
        return makeMove(gameState, gameState.selectedRow, gameState.selectedCol, 0)
    }
    
    /**
     * Get a hint for the selected cell.
     */
    fun getHint(gameState: SudokuGameState): SudokuGameState {
        if (gameState.selectedRow == -1 || gameState.selectedCol == -1) {
            return gameState
        }
        
        val row = gameState.selectedRow
        val col = gameState.selectedCol
        val cell = gameState.grid[row][col]
        
        if (cell.isGiven || cell.value != 0) {
            return gameState
        }
        
        // Find the correct number for this cell
        val correctNumber = findCorrectNumber(gameState.grid, row, col)
        
        return if (correctNumber != null) {
            makeMove(gameState, row, col, correctNumber)
        } else {
            gameState
        }
    }
    
    // Private helper methods
    
    private fun generateCompleteSolution(): List<List<Int>> {
        // For simplicity, start with a pre-defined valid solution
        // In a more advanced implementation, you could generate random solutions
        return listOf(
            listOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
            listOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
            listOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            listOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
            listOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
            listOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            listOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
            listOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
            listOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
        )
    }
    
    private fun removeNumbers(solution: List<List<Int>>, cellsToRemove: Int): List<List<SudokuCell>> {
        val grid = solution.map { row ->
            row.map { value ->
                SudokuCell(value = value, isGiven = true)
            }
        }.toMutableList()
        
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) {
            for (c in 0..8) {
                positions.add(Pair(r, c))
            }
        }
        positions.shuffle()
        
        repeat(cellsToRemove) { index ->
            if (index < positions.size) {
                val (r, c) = positions[index]
                grid[r] = grid[r].toMutableList().apply {
                    this[c] = SudokuCell(value = 0, isGiven = false)
                }
            }
        }
        
        return grid
    }
    
    private fun isValidState(grid: List<List<SudokuCell>>): Boolean {
        // Check rows
        for (row in 0..8) {
            val numbers = grid[row].mapNotNull { if (it.value > 0) it.value else null }
            if (numbers.size != numbers.toSet().size) return false
        }
        
        // Check columns
        for (col in 0..8) {
            val numbers = grid.mapNotNull { row -> 
                if (row[col].value > 0) row[col].value else null 
            }
            if (numbers.size != numbers.toSet().size) return false
        }
        
        // Check 3x3 boxes
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val numbers = mutableListOf<Int>()
                for (r in boxRow * 3..(boxRow * 3 + 2)) {
                    for (c in boxCol * 3..(boxCol * 3 + 2)) {
                        if (grid[r][c].value > 0) {
                            numbers.add(grid[r][c].value)
                        }
                    }
                }
                if (numbers.size != numbers.toSet().size) return false
            }
        }
        
        return true
    }
    
    private fun isCompleted(grid: List<List<SudokuCell>>): Boolean {
        // Check if all cells are filled and valid
        return grid.all { row -> row.all { cell -> cell.value in 1..9 } } && isValidState(grid)
    }
    
    private fun findCorrectNumber(grid: List<List<SudokuCell>>, row: Int, col: Int): Int? {
        val usedNumbers = mutableSetOf<Int>()
        
        // Add numbers from same row
        grid[row].forEach { cell ->
            if (cell.value > 0) usedNumbers.add(cell.value)
        }
        
        // Add numbers from same column
        for (r in 0..8) {
            if (grid[r][col].value > 0) {
                usedNumbers.add(grid[r][col].value)
            }
        }
        
        // Add numbers from same 3x3 box
        val boxRow = row / 3
        val boxCol = col / 3
        for (r in boxRow * 3..(boxRow * 3 + 2)) {
            for (c in boxCol * 3..(boxCol * 3 + 2)) {
                if (grid[r][c].value > 0) {
                    usedNumbers.add(grid[r][c].value)
                }
            }
        }
        
        // Find the missing number
        for (num in 1..9) {
            if (num !in usedNumbers) {
                return num
            }
        }
        
        return null
    }
}

/**
 * Sudoku difficulty levels.
 */
enum class SudokuDifficulty {
    EASY,
    MEDIUM, 
    HARD
}