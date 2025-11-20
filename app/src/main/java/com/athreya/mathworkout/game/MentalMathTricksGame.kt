package com.athreya.mathworkout.game

import kotlin.random.Random

/**
 * Mental Math Trick types
 */
enum class MathTrick {
    MULTIPLY_BY_11,      // 23 × 11 = 253 (2_3, middle = 2+3)
    SQUARE_ENDING_5,     // 25² = 625 (2×3=6, append 25)
    MULTIPLY_BY_9,       // 9 × 7 = 63 (fingers trick)
    DIVIDE_BY_5,         // Quick: multiply by 2, divide by 10
    MULTIPLY_BY_5,       // Quick: divide by 2, multiply by 10
    SQUARE_NEAR_50,      // 48² = (50-2)² = 2500-200+4
    PERCENTAGE_TRICK,    // 15% of 80 = 10% + 5%
    COMPLEMENT_TO_100    // 100 - 37 = 63 (complement)
}

/**
 * Mental Math Trick question
 */
data class MentalMathQuestion(
    val question: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val trick: MathTrick,
    val explanation: String
) {
    val hint: String
        get() = when (trick) {
            MathTrick.MULTIPLY_BY_11 -> "For a×11: write a, then middle digit is sum of a's digits, then a again"
            MathTrick.SQUARE_ENDING_5 -> "For n5²: multiply first digit by (digit+1), append 25"
            MathTrick.MULTIPLY_BY_9 -> "Hold up 10 fingers, put down the nth finger. Left = tens, right = ones"
            MathTrick.DIVIDE_BY_5 -> "Multiply by 2, then divide by 10 (move decimal left)"
            MathTrick.MULTIPLY_BY_5 -> "Divide by 2, then multiply by 10 (move decimal right)"
            MathTrick.SQUARE_NEAR_50 -> "Use (50-x)² = 2500 - 100x + x²"
            MathTrick.PERCENTAGE_TRICK -> "Break into 10% pieces: 10% is easy, then add/subtract"
            MathTrick.COMPLEMENT_TO_100 -> "For each digit: 9-digit, except last: 10-digit"
        }
}

/**
 * Generates Mental Math Trick questions
 */
object MentalMathTrickGenerator {
    
    fun generateQuestion(difficulty: String): MentalMathQuestion {
        val trick = when (difficulty) {
            "Easy" -> listOf(MathTrick.MULTIPLY_BY_11, MathTrick.MULTIPLY_BY_5, MathTrick.COMPLEMENT_TO_100).random()
            "Medium" -> listOf(MathTrick.SQUARE_ENDING_5, MathTrick.MULTIPLY_BY_9, MathTrick.DIVIDE_BY_5).random()
            else -> MathTrick.values().random()
        }
        
        return when (trick) {
            MathTrick.MULTIPLY_BY_11 -> generateMultiplyBy11()
            MathTrick.SQUARE_ENDING_5 -> generateSquareEnding5()
            MathTrick.MULTIPLY_BY_9 -> generateMultiplyBy9()
            MathTrick.DIVIDE_BY_5 -> generateDivideBy5()
            MathTrick.MULTIPLY_BY_5 -> generateMultiplyBy5()
            MathTrick.SQUARE_NEAR_50 -> generateSquareNear50()
            MathTrick.PERCENTAGE_TRICK -> generatePercentageTrick()
            MathTrick.COMPLEMENT_TO_100 -> generateComplementTo100()
        }
    }
    
    private fun generateMultiplyBy11(): MentalMathQuestion {
        val num = Random.nextInt(12, 99)
        val answer = num * 11
        val question = "$num × 11 ="
        
        val tens = num / 10
        val ones = num % 10
        val explanation = "Write $tens, then ${tens + ones}, then $ones = $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 20),
            trick = MathTrick.MULTIPLY_BY_11,
            explanation = explanation
        )
    }
    
    private fun generateSquareEnding5(): MentalMathQuestion {
        val tens = Random.nextInt(1, 10)
        val num = tens * 10 + 5
        val answer = num * num
        val question = "$num² ="
        
        val firstPart = tens * (tens + 1)
        val explanation = "$tens × ${tens + 1} = $firstPart, append 25 = $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 100),
            trick = MathTrick.SQUARE_ENDING_5,
            explanation = explanation
        )
    }
    
    private fun generateMultiplyBy9(): MentalMathQuestion {
        val num = Random.nextInt(2, 11)
        val answer = num * 9
        val question = "$num × 9 ="
        
        val tens = num - 1
        val ones = 9 - tens
        val explanation = "Finger trick: ${tens} tens, ${ones} ones = $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 10),
            trick = MathTrick.MULTIPLY_BY_9,
            explanation = explanation
        )
    }
    
    private fun generateDivideBy5(): MentalMathQuestion {
        val num = Random.nextInt(10, 50) * 5
        val answer = num / 5
        val question = "$num ÷ 5 ="
        
        val explanation = "$num × 2 = ${num * 2}, ÷ 10 = $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 5),
            trick = MathTrick.DIVIDE_BY_5,
            explanation = explanation
        )
    }
    
    private fun generateMultiplyBy5(): MentalMathQuestion {
        val num = Random.nextInt(12, 40)
        val answer = num * 5
        val question = "$num × 5 ="
        
        val half = if (num % 2 == 0) num / 2 else num
        val explanation = if (num % 2 == 0) {
            "$num ÷ 2 = $half, × 10 = $answer"
        } else {
            "$num ÷ 2 = ${num.toFloat() / 2}, × 10 = $answer"
        }
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 10),
            trick = MathTrick.MULTIPLY_BY_5,
            explanation = explanation
        )
    }
    
    private fun generateSquareNear50(): MentalMathQuestion {
        val diff = Random.nextInt(1, 10)
        val num = 50 - diff
        val answer = num * num
        val question = "$num² ="
        
        val explanation = "(50-$diff)² = 2500 - ${100 * diff} + ${diff * diff} = $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 50),
            trick = MathTrick.SQUARE_NEAR_50,
            explanation = explanation
        )
    }
    
    private fun generatePercentageTrick(): MentalMathQuestion {
        val percent = listOf(15, 25, 35).random()
        val num = Random.nextInt(20, 100)
        val answer = (num * percent) / 100
        val question = "$percent% of $num ="
        
        val ten = num / 10
        val explanation = when (percent) {
            15 -> "10% = $ten, 5% = ${ten / 2}, total = $answer"
            25 -> "25% = $num ÷ 4 = $answer"
            35 -> "10% = $ten, 30% = ${ten * 3}, 5% = ${ten / 2}, total = $answer"
            else -> ""
        }
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 5),
            trick = MathTrick.PERCENTAGE_TRICK,
            explanation = explanation
        )
    }
    
    private fun generateComplementTo100(): MentalMathQuestion {
        val num = Random.nextInt(21, 99)
        val answer = 100 - num
        val question = "100 - $num ="
        
        val tens = num / 10
        val ones = num % 10
        val newTens = 9 - tens
        val newOnes = 10 - ones
        val explanation = "9-$tens=${newTens}, 10-$ones=${newOnes} → $answer"
        
        return MentalMathQuestion(
            question = question,
            correctAnswer = answer,
            options = generateOptions(answer, 5),
            trick = MathTrick.COMPLEMENT_TO_100,
            explanation = explanation
        )
    }
    
    private fun generateOptions(correctAnswer: Int, spread: Int): List<Int> {
        val options = mutableSetOf(correctAnswer)
        
        while (options.size < 4) {
            val offset = Random.nextInt(-spread, spread + 1)
            val option = maxOf(1, correctAnswer + offset)
            if (option != correctAnswer) {
                options.add(option)
            }
        }
        
        return options.shuffled()
    }
}
