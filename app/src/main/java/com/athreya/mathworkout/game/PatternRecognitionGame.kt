package com.athreya.mathworkout.game

import kotlin.random.Random

/**
 * Pattern Recognition question types
 */
enum class PatternType {
    ARITHMETIC,      // Linear sequence (2, 4, 6, 8...)
    GEOMETRIC,       // Multiplicative (2, 4, 8, 16...)
    FIBONACCI,       // Sum of previous two (1, 1, 2, 3, 5...)
    SQUARE_NUMBERS,  // Perfect squares (1, 4, 9, 16...)
    PRIME_NUMBERS,   // Prime sequence (2, 3, 5, 7, 11...)
    CUSTOM          // Special patterns
}

/**
 * Pattern Recognition question
 */
data class PatternQuestion(
    val sequence: List<Int>,
    val missingIndex: Int,  // -1 for "what comes next"
    val correctAnswer: Int,
    val options: List<Int>,
    val patternType: PatternType,
    val difficulty: String
) {
    val hint: String
        get() = when (patternType) {
            PatternType.ARITHMETIC -> "Look at the difference between consecutive numbers"
            PatternType.GEOMETRIC -> "Each number is multiplied by something"
            PatternType.FIBONACCI -> "Each number is the sum of the previous two"
            PatternType.SQUARE_NUMBERS -> "Think about perfect squares (1², 2², 3²...)"
            PatternType.PRIME_NUMBERS -> "These are prime numbers (only divisible by 1 and themselves)"
            PatternType.CUSTOM -> "Look for a repeating pattern or rule"
        }
    
    fun getQuestionText(): String {
        return if (missingIndex == -1) {
            "What comes next? ${sequence.joinToString(", ")}, ?"
        } else {
            val seqWithGap = sequence.toMutableList()
            seqWithGap[missingIndex] = -999 // Placeholder
            seqWithGap.joinToString(", ") { if (it == -999) "?" else it.toString() }
        }
    }
}

/**
 * Generates Pattern Recognition questions
 */
object PatternRecognitionGenerator {
    
    fun generateQuestion(difficulty: String): PatternQuestion {
        val patternType = when (difficulty) {
            "Easy" -> listOf(PatternType.ARITHMETIC, PatternType.SQUARE_NUMBERS).random()
            "Medium" -> listOf(PatternType.ARITHMETIC, PatternType.GEOMETRIC, PatternType.SQUARE_NUMBERS).random()
            else -> PatternType.values().filter { it != PatternType.CUSTOM }.random()
        }
        
        return when (patternType) {
            PatternType.ARITHMETIC -> generateArithmeticPattern(difficulty)
            PatternType.GEOMETRIC -> generateGeometricPattern(difficulty)
            PatternType.FIBONACCI -> generateFibonacciPattern(difficulty)
            PatternType.SQUARE_NUMBERS -> generateSquarePattern(difficulty)
            PatternType.PRIME_NUMBERS -> generatePrimePattern(difficulty)
            PatternType.CUSTOM -> generateCustomPattern(difficulty)
        }
    }
    
    private fun generateArithmeticPattern(difficulty: String): PatternQuestion {
        val step = when (difficulty) {
            "Easy" -> listOf(2, 3, 5, 10).random()
            "Medium" -> Random.nextInt(2, 15)
            else -> Random.nextInt(5, 25)
        }
        
        val start = Random.nextInt(1, 30)
        val length = 5
        val sequence = (0 until length).map { start + it * step }
        
        val missingIndex = if (difficulty == "Easy") -1 else Random.nextInt(length)
        val answer = if (missingIndex == -1) start + length * step else sequence[missingIndex]
        
        val displaySeq = if (missingIndex == -1) sequence else sequence.filterIndexed { i, _ -> i != missingIndex }
        
        return PatternQuestion(
            sequence = displaySeq,
            missingIndex = missingIndex,
            correctAnswer = answer,
            options = generateOptions(answer, step),
            patternType = PatternType.ARITHMETIC,
            difficulty = difficulty
        )
    }
    
    private fun generateGeometricPattern(difficulty: String): PatternQuestion {
        val ratio = when (difficulty) {
            "Easy" -> listOf(2, 3).random()
            "Medium" -> listOf(2, 3, 4).random()
            else -> Random.nextInt(2, 6)
        }
        
        val start = when (difficulty) {
            "Easy" -> Random.nextInt(1, 5)
            "Medium" -> Random.nextInt(1, 10)
            else -> Random.nextInt(2, 15)
        }
        
        val length = 5
        val sequence = (0 until length).map { start * Math.pow(ratio.toDouble(), it.toDouble()).toInt() }
        
        val missingIndex = if (difficulty == "Easy") -1 else Random.nextInt(length)
        val answer = if (missingIndex == -1) start * Math.pow(ratio.toDouble(), length.toDouble()).toInt() else sequence[missingIndex]
        
        val displaySeq = if (missingIndex == -1) sequence else sequence.filterIndexed { i, _ -> i != missingIndex }
        
        return PatternQuestion(
            sequence = displaySeq,
            missingIndex = missingIndex,
            correctAnswer = answer,
            options = generateOptions(answer, answer / 3),
            patternType = PatternType.GEOMETRIC,
            difficulty = difficulty
        )
    }
    
    private fun generateFibonacciPattern(difficulty: String): PatternQuestion {
        val fibonacci = mutableListOf(1, 1)
        while (fibonacci.size < 6) {
            fibonacci.add(fibonacci[fibonacci.size - 1] + fibonacci[fibonacci.size - 2])
        }
        
        val displaySeq = fibonacci.take(5)
        val answer = fibonacci[5]
        
        return PatternQuestion(
            sequence = displaySeq,
            missingIndex = -1,
            correctAnswer = answer,
            options = generateOptions(answer, 3),
            patternType = PatternType.FIBONACCI,
            difficulty = difficulty
        )
    }
    
    private fun generateSquarePattern(difficulty: String): PatternQuestion {
        val start = if (difficulty == "Easy") 1 else Random.nextInt(1, 5)
        val length = 5
        val sequence = (start until start + length).map { it * it }
        
        val missingIndex = if (difficulty == "Easy") -1 else Random.nextInt(length)
        val answer = if (missingIndex == -1) (start + length) * (start + length) else sequence[missingIndex]
        
        val displaySeq = if (missingIndex == -1) sequence else sequence.filterIndexed { i, _ -> i != missingIndex }
        
        return PatternQuestion(
            sequence = displaySeq,
            missingIndex = missingIndex,
            correctAnswer = answer,
            options = generateOptions(answer, 5),
            patternType = PatternType.SQUARE_NUMBERS,
            difficulty = difficulty
        )
    }
    
    private fun generatePrimePattern(difficulty: String): PatternQuestion {
        val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)
        val displaySeq = primes.take(5)
        val answer = primes[5]
        
        return PatternQuestion(
            sequence = displaySeq,
            missingIndex = -1,
            correctAnswer = answer,
            options = listOf(13, 14, 15, 16).shuffled(),
            patternType = PatternType.PRIME_NUMBERS,
            difficulty = difficulty
        )
    }
    
    private fun generateCustomPattern(difficulty: String): PatternQuestion {
        // Alternating pattern: +2, +3, +2, +3...
        val start = Random.nextInt(1, 20)
        val sequence = mutableListOf(start)
        var add2 = true
        for (i in 1 until 5) {
            sequence.add(sequence.last() + if (add2) 2 else 3)
            add2 = !add2
        }
        
        val answer = sequence.last() + if (sequence.size % 2 == 0) 3 else 2
        
        return PatternQuestion(
            sequence = sequence,
            missingIndex = -1,
            correctAnswer = answer,
            options = generateOptions(answer, 2),
            patternType = PatternType.CUSTOM,
            difficulty = difficulty
        )
    }
    
    private fun generateOptions(correctAnswer: Int, spread: Int): List<Int> {
        val options = mutableSetOf(correctAnswer)
        
        while (options.size < 4) {
            val offset = Random.nextInt(-spread * 2, spread * 2 + 1)
            val option = maxOf(1, correctAnswer + offset)
            if (option != correctAnswer) {
                options.add(option)
            }
        }
        
        return options.shuffled()
    }
}
