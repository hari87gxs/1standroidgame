package com.athreya.mathworkout.game

import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import kotlin.random.Random

/**
 * Data class representing a single math question.
 * 
 * @param question The question text to display (e.g., "15 + 22 = ?")
 * @param correctAnswer The correct numerical answer
 * @param operation The mathematical operation used (for tracking)
 * @param expectedDigits The expected number of digits in the answer (for auto-submission)
 */
data class MathQuestion(
    val question: String,
    val correctAnswer: Int,
    val operation: String,
    val expectedDigits: Int = correctAnswer.toString().length
)

/**
 * QuestionGenerator is responsible for creating math questions based on
 * the selected game mode and difficulty level.
 * 
 * This class encapsulates all the logic for generating different types
 * of mathematical problems.
 */
class QuestionGenerator {
    
    /**
     * Generate a random math question based on game mode and difficulty.
     * 
     * @param gameMode The type of mathematical operations to include
     * @param difficulty The complexity level (affects number ranges)
     * @return A MathQuestion object containing the question and answer
     */
    fun generateQuestion(gameMode: GameMode, difficulty: Difficulty): MathQuestion {
        return when (gameMode) {
            GameMode.ADDITION_SUBTRACTION -> generateAdditionSubtraction(difficulty)
            GameMode.MULTIPLICATION_DIVISION -> generateMultiplicationDivision(difficulty)
            GameMode.TEST_ME -> generateMixedOperation(difficulty)
            GameMode.BRAIN_TEASER -> generateBrainTeaser(difficulty)
            GameMode.SUDOKU -> throw IllegalArgumentException("Sudoku mode does not use QuestionGenerator")
        }
    }
    
    /**
     * Generate addition or subtraction problems.
     */
    private fun generateAdditionSubtraction(difficulty: Difficulty): MathQuestion {
        val range = getNumberRange(difficulty)
        val num1 = Random.nextInt(1, range + 1)
        val num2 = Random.nextInt(1, range + 1)
        
        return if (Random.nextBoolean()) {
            // Addition
            MathQuestion(
                question = "$num1 + $num2 = ?",
                correctAnswer = num1 + num2,
                operation = "Addition"
            )
        } else {
            // Subtraction (ensure positive result)
            val larger = maxOf(num1, num2)
            val smaller = minOf(num1, num2)
            MathQuestion(
                question = "$larger - $smaller = ?",
                correctAnswer = larger - smaller,
                operation = "Subtraction"
            )
        }
    }
    
    /**
     * Generate multiplication or division problems.
     */
    private fun generateMultiplicationDivision(difficulty: Difficulty): MathQuestion {
        val range = getNumberRange(difficulty)
        
        return if (Random.nextBoolean()) {
            // Multiplication
            val num1 = Random.nextInt(1, if (difficulty == Difficulty.EASY) 11 else 21)
            val num2 = Random.nextInt(1, if (difficulty == Difficulty.EASY) 11 else 21)
            MathQuestion(
                question = "$num1 × $num2 = ?",
                correctAnswer = num1 * num2,
                operation = "Multiplication"
            )
        } else {
            // Division (ensure whole number result)
            val divisor = Random.nextInt(2, if (difficulty == Difficulty.EASY) 11 else 16)
            val quotient = Random.nextInt(1, range / divisor + 1)
            val dividend = divisor * quotient
            MathQuestion(
                question = "$dividend ÷ $divisor = ?",
                correctAnswer = quotient,
                operation = "Division"
            )
        }
    }
    
    /**
     * Generate mixed operations (any of the four basic operations).
     */
    private fun generateMixedOperation(difficulty: Difficulty): MathQuestion {
        return when (Random.nextInt(4)) {
            0, 1 -> generateAdditionSubtraction(difficulty)
            2, 3 -> generateMultiplicationDivision(difficulty)
            else -> generateAdditionSubtraction(difficulty)
        }
    }
    
    /**
     * Generate brain teaser problems with multiple operations.
     * These follow order of operations (multiplication/division before addition/subtraction).
     */
    private fun generateBrainTeaser(difficulty: Difficulty): MathQuestion {
        val range = when (difficulty) {
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 20
            Difficulty.COMPLEX -> 50
        }
        
        val operations = listOf(
            generateTwoStepProblem(range),
            generateThreeStepProblem(range)
        )
        
        return operations[Random.nextInt(operations.size)]
    }
    
    /**
     * Generate a two-step brain teaser (e.g., "5 + 3 × 2").
     */
    private fun generateTwoStepProblem(range: Int): MathQuestion {
        val a = Random.nextInt(1, range + 1)
        val b = Random.nextInt(1, 11) // Keep multiplier small
        val c = Random.nextInt(1, range + 1)
        
        return if (Random.nextBoolean()) {
            // Addition and multiplication: a + b × c
            val result = a + (b * c)
            MathQuestion(
                question = "$a + $b × $c = ?",
                correctAnswer = result,
                operation = "Brain Teaser"
            )
        } else {
            // Subtraction and multiplication: a - b × c (ensure positive result)
            val product = b * c
            val minuend = product + Random.nextInt(1, range + 1)
            val result = minuend - product
            MathQuestion(
                question = "$minuend - $b × $c = ?",
                correctAnswer = result,
                operation = "Brain Teaser"
            )
        }
    }
    
    /**
     * Generate a three-step brain teaser (e.g., "10 + 6 ÷ 2 - 3").
     */
    private fun generateThreeStepProblem(range: Int): MathQuestion {
        val divisor = Random.nextInt(2, 6)
        val quotient = Random.nextInt(1, 11)
        val dividend = divisor * quotient
        val addend = Random.nextInt(1, range + 1)
        val subtrahend = Random.nextInt(1, 6)
        
        // Format: a + b ÷ c - d
        val result = addend + quotient - subtrahend
        
        // Ensure positive result
        if (result > 0) {
            return MathQuestion(
                question = "$addend + $dividend ÷ $divisor - $subtrahend = ?",
                correctAnswer = result,
                operation = "Brain Teaser"
            )
        } else {
            // Fallback to simpler problem
            return generateTwoStepProblem(range)
        }
    }
    
    /**
     * Get the appropriate number range based on difficulty level.
     */
    private fun getNumberRange(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 100
            Difficulty.COMPLEX -> 1000
        }
    }
}