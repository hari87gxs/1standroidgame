package com.athreya.mathworkout.game

import kotlin.random.Random

/**
 * Target Number game mode where players combine given numbers using operations
 * to reach a target number.
 */
data class TargetNumberQuestion(
    val availableNumbers: List<Int>,
    val targetNumber: Int,
    val difficulty: String,
    var playerSolution: String = "",
    var isCorrect: Boolean = false
) {
    val hint: String
        get() = when (difficulty) {
            "Easy" -> "Try combining ${availableNumbers.take(2)} first"
            "Medium" -> "Look for factors of $targetNumber in your numbers"
            else -> "Work backwards from $targetNumber"
        }
}

/**
 * Generates Target Number questions based on difficulty
 */
object TargetNumberGenerator {
    
    fun generateQuestion(difficulty: String): TargetNumberQuestion {
        return when (difficulty) {
            "Easy" -> generateEasyQuestion()
            "Medium" -> generateMediumQuestion()
            else -> generateHardQuestion()
        }
    }
    
    private fun generateEasyQuestion(): TargetNumberQuestion {
        // 3 numbers, simple target
        val numbers = List(3) { Random.nextInt(1, 20) }
        val target = calculateSimpleTarget(numbers)
        return TargetNumberQuestion(numbers, target, "Easy")
    }
    
    private fun generateMediumQuestion(): TargetNumberQuestion {
        // 4-5 numbers, moderate target
        val count = Random.nextInt(4, 6)
        val numbers = List(count) { Random.nextInt(1, 50) }
        val target = calculateModerateTarget(numbers)
        return TargetNumberQuestion(numbers, target, "Medium")
    }
    
    private fun generateHardQuestion(): TargetNumberQuestion {
        // 6 numbers with common game numbers
        val commonNumbers = listOf(25, 50, 75, 100)
        val smallNumbers = List(4) { Random.nextInt(1, 11) }
        val largeNumbers = commonNumbers.shuffled().take(2)
        val numbers = (smallNumbers + largeNumbers).shuffled()
        val target = Random.nextInt(100, 1000)
        return TargetNumberQuestion(numbers, target, "Hard")
    }
    
    private fun calculateSimpleTarget(numbers: List<Int>): Int {
        // Create a solvable target using 2-3 numbers
        return when (Random.nextInt(3)) {
            0 -> numbers[0] + numbers[1]
            1 -> numbers[0] * numbers[1]
            else -> numbers[0] + numbers[1] + numbers[2]
        }
    }
    
    private fun calculateModerateTarget(numbers: List<Int>): Int {
        // Create a solvable target using combinations
        return when (Random.nextInt(4)) {
            0 -> numbers[0] + numbers[1] * numbers[2]
            1 -> (numbers[0] + numbers[1]) * numbers[2]
            2 -> numbers[0] * numbers[1] + numbers[2]
            else -> numbers[0] + numbers[1] + numbers[2] * 2
        }
    }
    
    /**
     * Validates if the player's solution is correct
     */
    fun validateSolution(
        question: TargetNumberQuestion,
        solution: String,
        usedNumbers: List<Int>
    ): Boolean {
        // Check if all used numbers are from available numbers
        val available = question.availableNumbers.toMutableList()
        for (num in usedNumbers) {
            if (!available.remove(num)) {
                return false // Number used more than available
            }
        }
        
        // Evaluate the expression
        return try {
            val result = evaluateExpression(solution)
            result == question.targetNumber
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Simple expression evaluator
     */
    private fun evaluateExpression(expr: String): Int {
        // Remove spaces
        val cleanExpr = expr.replace(" ", "")
        
        // Simple stack-based evaluation (supports +, -, *, /)
        val numbers = mutableListOf<Int>()
        val operators = mutableListOf<Char>()
        var currentNumber = ""
        
        for (char in cleanExpr) {
            when {
                char.isDigit() -> currentNumber += char
                char in "+-*/" -> {
                    if (currentNumber.isNotEmpty()) {
                        numbers.add(currentNumber.toInt())
                        currentNumber = ""
                    }
                    operators.add(char)
                }
            }
        }
        if (currentNumber.isNotEmpty()) {
            numbers.add(currentNumber.toInt())
        }
        
        // Process multiplication and division first
        var i = 0
        while (i < operators.size) {
            if (operators[i] in "*/") {
                val result = when (operators[i]) {
                    '*' -> numbers[i] * numbers[i + 1]
                    '/' -> numbers[i] / numbers[i + 1]
                    else -> 0
                }
                numbers[i] = result
                numbers.removeAt(i + 1)
                operators.removeAt(i)
            } else {
                i++
            }
        }
        
        // Process addition and subtraction
        var result = numbers[0]
        for (j in operators.indices) {
            result = when (operators[j]) {
                '+' -> result + numbers[j + 1]
                '-' -> result - numbers[j + 1]
                else -> result
            }
        }
        
        return result
    }
}
