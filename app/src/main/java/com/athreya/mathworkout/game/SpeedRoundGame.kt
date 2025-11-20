package com.athreya.mathworkout.game

import kotlin.random.Random

/**
 * Speed Round question - simple, quick calculations
 */
data class SpeedRoundQuestion(
    val question: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val difficulty: Int  // Increases as game progresses
) {
    val hint: String
        get() = when {
            question.contains("+") -> "Think: Add the numbers quickly"
            question.contains("-") -> "Think: Subtract from left to right"
            question.contains("×") -> "Remember your times tables"
            question.contains("÷") -> "Think: Division fact"
            else -> "Calculate step by step"
        }
}

/**
 * Generates rapid-fire Speed Round questions
 */
object SpeedRoundGenerator {
    
    fun generateQuestion(round: Int): SpeedRoundQuestion {
        val difficulty = minOf(round / 5, 4) // Max difficulty level 4
        
        return when (Random.nextInt(4)) {
            0 -> generateAdditionQuestion(difficulty)
            1 -> generateSubtractionQuestion(difficulty)
            2 -> generateMultiplicationQuestion(difficulty)
            else -> generateDivisionQuestion(difficulty)
        }
    }
    
    private fun generateAdditionQuestion(difficulty: Int): SpeedRoundQuestion {
        val range = when (difficulty) {
            0 -> 1..10
            1 -> 1..20
            2 -> 10..50
            3 -> 50..100
            else -> 100..200
        }
        
        val a = Random.nextInt(range.first, range.last)
        val b = Random.nextInt(range.first, range.last)
        val answer = a + b
        val question = "$a + $b ="
        
        return SpeedRoundQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer),
            difficulty = difficulty
        )
    }
    
    private fun generateSubtractionQuestion(difficulty: Int): SpeedRoundQuestion {
        val range = when (difficulty) {
            0 -> 10..20
            1 -> 20..40
            2 -> 30..80
            3 -> 80..150
            else -> 150..300
        }
        
        val a = Random.nextInt(range.first, range.last)
        val b = Random.nextInt(range.first / 2, a)
        val answer = a - b
        val question = "$a - $b ="
        
        return SpeedRoundQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer),
            difficulty = difficulty
        )
    }
    
    private fun generateMultiplicationQuestion(difficulty: Int): SpeedRoundQuestion {
        val range = when (difficulty) {
            0 -> 1..5
            1 -> 1..10
            2 -> 2..12
            3 -> 5..15
            else -> 10..20
        }
        
        val a = Random.nextInt(range.first, range.last)
        val b = Random.nextInt(range.first, range.last)
        val answer = a * b
        val question = "$a × $b ="
        
        return SpeedRoundQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer),
            difficulty = difficulty
        )
    }
    
    private fun generateDivisionQuestion(difficulty: Int): SpeedRoundQuestion {
        val divisorRange = when (difficulty) {
            0 -> 2..5
            1 -> 2..10
            2 -> 3..12
            3 -> 5..15
            else -> 8..20
        }
        
        val divisor = Random.nextInt(divisorRange.first, divisorRange.last)
        val quotient = Random.nextInt(2, 15)
        val dividend = divisor * quotient
        val question = "$dividend ÷ $divisor ="
        
        return SpeedRoundQuestion(
            question = question,
            correctAnswer = quotient,
            options = generateOptions(quotient),
            difficulty = difficulty
        )
    }
    
    private fun generateOptions(correctAnswer: Int): List<Int> {
        val options = mutableSetOf(correctAnswer)
        val range = maxOf(5, correctAnswer / 5)
        
        while (options.size < 4) {
            val offset = Random.nextInt(-range, range + 1)
            val option = maxOf(0, correctAnswer + offset)
            if (option != correctAnswer) {
                options.add(option)
            }
        }
        
        return options.shuffled()
    }
    
    /**
     * Calculate score based on time taken
     * Faster answers get more points
     */
    fun calculateScore(timeInSeconds: Float, wasCorrect: Boolean): Int {
        if (!wasCorrect) return 0
        
        return when {
            timeInSeconds < 2 -> 100
            timeInSeconds < 4 -> 75
            timeInSeconds < 6 -> 50
            timeInSeconds < 10 -> 25
            else -> 10
        }
    }
}
