package com.athreya.mathworkout.data

import kotlin.random.Random

/**
 * Types of interactive math games
 */
enum class GameType {
    NUMBER_PUZZLE,      // Fill in missing numbers in a pattern
    PATTERN_MATCHING,   // Identify the pattern and predict next
    STORY_PROBLEM,      // Word problems with real-world context
    NUMBER_SEQUENCE,    // Find the next number in sequence
    MATH_RIDDLE        // Brain teaser riddles
}

/**
 * Difficulty level for games
 */
enum class GameDifficulty {
    EASY,
    MEDIUM,
    HARD
}

/**
 * Represents a math game/puzzle
 */
data class MathGame(
    val id: String,
    val type: GameType,
    val difficulty: GameDifficulty,
    val question: String,
    val choices: List<String> = emptyList(), // Multiple choice options
    val correctAnswer: String,
    val explanation: String,
    val hint: String = "",
    val emoji: String = "🎮"
)

/**
 * Daily riddle data
 */
data class DailyRiddle(
    val id: String,
    val riddle: String,
    val answer: String,
    val explanation: String,
    val emoji: String = "🤔",
    val date: String // Format: YYYY-MM-DD
)

/**
 * Repository for interactive math games
 */
object MathGames {
    
    /**
     * Generate a number puzzle game
     */
    fun generateNumberPuzzle(difficulty: GameDifficulty): MathGame {
        return when (difficulty) {
            GameDifficulty.EASY -> {
                // Simple addition/subtraction patterns
                val start = Random.nextInt(1, 10)
                val step = Random.nextInt(1, 5)
                val sequence = listOf(start, start + step, start + step * 2, "?", start + step * 4)
                val answer = (start + step * 3).toString()
                
                MathGame(
                    id = "puzzle_easy_${Random.nextInt(1000)}",
                    type = GameType.NUMBER_PUZZLE,
                    difficulty = GameDifficulty.EASY,
                    question = "What number comes next?\n${sequence.joinToString(" → ")}",
                    choices = listOf(
                        answer,
                        (start + step * 3 + 1).toString(),
                        (start + step * 3 - 1).toString(),
                        (start + step * 2).toString()
                    ).shuffled(),
                    correctAnswer = answer,
                    explanation = "The pattern adds $step each time: $start, ${start + step}, ${start + step * 2}, ${start + step * 3}",
                    hint = "Look at the difference between consecutive numbers",
                    emoji = "🧩"
                )
            }
            GameDifficulty.MEDIUM -> {
                // Multiplication patterns
                val start = Random.nextInt(2, 6)
                val multiplier = Random.nextInt(2, 4)
                val sequence = listOf(start, start * multiplier, start * multiplier * multiplier, "?")
                val answer = (start * multiplier * multiplier * multiplier).toString()
                
                MathGame(
                    id = "puzzle_medium_${Random.nextInt(1000)}",
                    type = GameType.NUMBER_PUZZLE,
                    difficulty = GameDifficulty.MEDIUM,
                    question = "Find the missing number:\n${sequence.joinToString(" → ")}",
                    choices = listOf(
                        answer,
                        (start * multiplier * multiplier * multiplier + multiplier).toString(),
                        (start * multiplier * multiplier).toString(),
                        (start * multiplier * 4).toString()
                    ).shuffled(),
                    correctAnswer = answer,
                    explanation = "Each number is multiplied by $multiplier",
                    hint = "Try dividing consecutive numbers",
                    emoji = "🧩"
                )
            }
            GameDifficulty.HARD -> {
                // Complex patterns (Fibonacci-like or alternating operations)
                val a = Random.nextInt(1, 5)
                val b = Random.nextInt(5, 10)
                val c = a + b
                val d = b + c
                val answer = (c + d).toString()
                
                MathGame(
                    id = "puzzle_hard_${Random.nextInt(1000)}",
                    type = GameType.NUMBER_PUZZLE,
                    difficulty = GameDifficulty.HARD,
                    question = "Complete the sequence:\n$a → $b → $c → $d → ?",
                    choices = listOf(
                        answer,
                        (d + a).toString(),
                        (d * 2).toString(),
                        (c + b).toString()
                    ).shuffled(),
                    correctAnswer = answer,
                    explanation = "Each number is the sum of the previous two numbers (like Fibonacci)",
                    hint = "Add the previous two numbers",
                    emoji = "🧩"
                )
            }
        }
    }
    
    /**
     * Generate a pattern matching game
     */
    fun generatePatternMatching(difficulty: GameDifficulty): MathGame {
        val patterns = when (difficulty) {
            GameDifficulty.EASY -> listOf(
                Triple("Even numbers", listOf(2, 4, 6, 8, "?"), "10"),
                Triple("Odd numbers", listOf(1, 3, 5, 7, "?"), "9"),
                Triple("Counting by 5", listOf(5, 10, 15, 20, "?"), "25")
            )
            GameDifficulty.MEDIUM -> listOf(
                Triple("Multiples of 3", listOf(3, 6, 9, 12, "?"), "15"),
                Triple("Powers of 2", listOf(2, 4, 8, 16, "?"), "32"),
                Triple("Square numbers", listOf(1, 4, 9, 16, "?"), "25")
            )
            GameDifficulty.HARD -> listOf(
                Triple("Prime numbers", listOf(2, 3, 5, 7, "?"), "11"),
                Triple("Triangular numbers", listOf(1, 3, 6, 10, "?"), "15"),
                Triple("Cube numbers", listOf(1, 8, 27, "?"), "64")
            )
        }
        
        val (patternName, sequence, answer) = patterns.random()
        val wrongAnswers = generateWrongAnswers(answer.toInt(), 3)
        
        return MathGame(
            id = "pattern_${difficulty.name.lowercase()}_${Random.nextInt(1000)}",
            type = GameType.PATTERN_MATCHING,
            difficulty = difficulty,
            question = "Identify the pattern:\n${sequence.joinToString(" , ")}",
            choices = (listOf(answer) + wrongAnswers).shuffled(),
            correctAnswer = answer,
            explanation = "This is the pattern: $patternName",
            hint = "Think about what makes these numbers special",
            emoji = "🔢"
        )
    }
    
    /**
     * Generate a story problem
     */
    fun generateStoryProblem(difficulty: GameDifficulty): MathGame {
        val stories = when (difficulty) {
            GameDifficulty.EASY -> listOf(
                {
                    val apples = Random.nextInt(3, 10)
                    val more = Random.nextInt(2, 5)
                    val answer = apples + more
                    MathGame(
                        id = "story_easy_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.EASY,
                        question = "🍎 Sarah has $apples apples. Her friend gives her $more more apples. How many apples does Sarah have now?",
                        choices = listOf(
                            answer.toString(),
                            (answer - 1).toString(),
                            (answer + 1).toString(),
                            (apples - more).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "Sarah started with $apples apples and got $more more, so $apples + $more = $answer apples",
                        hint = "When someone gives you more, you add!",
                        emoji = "📖"
                    )
                },
                {
                    val cookies = Random.nextInt(10, 20)
                    val eaten = Random.nextInt(3, 8)
                    val answer = cookies - eaten
                    MathGame(
                        id = "story_easy_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.EASY,
                        question = "🍪 Tom baked $cookies cookies. He ate $eaten cookies. How many cookies are left?",
                        choices = listOf(
                            answer.toString(),
                            (answer + 1).toString(),
                            (answer - 1).toString(),
                            (cookies + eaten).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "Tom had $cookies cookies and ate $eaten, so $cookies - $eaten = $answer cookies left",
                        hint = "When you eat cookies, subtract!",
                        emoji = "📖"
                    )
                }
            )
            GameDifficulty.MEDIUM -> listOf(
                {
                    val pricePerPen = Random.nextInt(2, 6)
                    val pens = Random.nextInt(3, 7)
                    val answer = pricePerPen * pens
                    MathGame(
                        id = "story_medium_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.MEDIUM,
                        question = "✏️ Each pen costs $$pricePerPen. How much do $pens pens cost?",
                        choices = listOf(
                            answer.toString(),
                            (answer + pricePerPen).toString(),
                            (answer - pricePerPen).toString(),
                            (pricePerPen + pens).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "$pens pens × $$pricePerPen each = $$answer total",
                        hint = "Multiply the number of items by the price of each",
                        emoji = "📖"
                    )
                },
                {
                    val totalStudents = Random.nextInt(20, 40)
                    val groups = Random.nextInt(4, 6)
                    val answer = totalStudents / groups
                    MathGame(
                        id = "story_medium_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.MEDIUM,
                        question = "👥 A class of $totalStudents students is divided into $groups equal groups. How many students in each group?",
                        choices = listOf(
                            answer.toString(),
                            (answer + 1).toString(),
                            (answer - 1).toString(),
                            (totalStudents - groups).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "$totalStudents students ÷ $groups groups = $answer students per group",
                        hint = "Divide the total by the number of groups",
                        emoji = "📖"
                    )
                }
            )
            GameDifficulty.HARD -> listOf(
                {
                    val length = Random.nextInt(8, 15)
                    val width = Random.nextInt(5, 10)
                    val answer = length * width
                    MathGame(
                        id = "story_hard_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.HARD,
                        question = "📐 A rectangular garden is ${length}m long and ${width}m wide. What is its area in square meters?",
                        choices = listOf(
                            answer.toString(),
                            (length + width).toString(),
                            ((length + width) * 2).toString(),
                            (answer + 10).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "Area = length × width = $length × $width = $answer square meters",
                        hint = "Multiply length times width for area",
                        emoji = "📖"
                    )
                },
                {
                    val total = Random.nextInt(40, 80)
                    val percentage = listOf(25, 50, 75).random()
                    val answer = (total * percentage) / 100
                    MathGame(
                        id = "story_hard_${Random.nextInt(1000)}",
                        type = GameType.STORY_PROBLEM,
                        difficulty = GameDifficulty.HARD,
                        question = "💰 A store has a $percentage% off sale. If a toy originally costs $$total, how much is the discount?",
                        choices = listOf(
                            answer.toString(),
                            (total - answer).toString(),
                            (answer + 5).toString(),
                            (total / 2).toString()
                        ).shuffled(),
                        correctAnswer = answer.toString(),
                        explanation = "$percentage% of $$total = ($$total × $percentage) ÷ 100 = $$answer",
                        hint = "Find $percentage% by dividing by 100 and multiplying",
                        emoji = "📖"
                    )
                }
            )
        }
        
        return stories.random().invoke()
    }
    
    /**
     * Get daily riddle for a specific date
     */
    fun getDailyRiddle(date: String): DailyRiddle {
        val riddles = listOf(
            DailyRiddle(
                id = "riddle_1",
                date = date,
                riddle = "I am an odd number. Take away one letter and I become even. What number am I?",
                answer = "Seven",
                explanation = "Remove the 's' from 'Seven' and you get 'even'!",
                emoji = "🤔"
            ),
            DailyRiddle(
                id = "riddle_2",
                date = date,
                riddle = "If you multiply this number by any other number, the answer will always be the same. What number is it?",
                answer = "Zero",
                explanation = "Any number multiplied by 0 equals 0!",
                emoji = "🤔"
            ),
            DailyRiddle(
                id = "riddle_3",
                date = date,
                riddle = "What has three feet but cannot walk?",
                answer = "A yardstick",
                explanation = "A yardstick is 3 feet long but can't walk!",
                emoji = "📏"
            ),
            DailyRiddle(
                id = "riddle_4",
                date = date,
                riddle = "If two's company and three's a crowd, what are four and five?",
                answer = "Nine",
                explanation = "4 + 5 = 9! It's a math riddle!",
                emoji = "🤔"
            ),
            DailyRiddle(
                id = "riddle_5",
                date = date,
                riddle = "What number do you get when you multiply all of the numbers on a telephone number pad?",
                answer = "Zero",
                explanation = "Because one of the numbers is 0, and anything × 0 = 0!",
                emoji = "📞"
            ),
            DailyRiddle(
                id = "riddle_6",
                date = date,
                riddle = "How can you add eight 8's to get the number 1,000?",
                answer = "888 + 88 + 8 + 8 + 8",
                explanation = "888 + 88 + 8 + 8 + 8 = 1,000",
                emoji = "🤔"
            ),
            DailyRiddle(
                id = "riddle_7",
                date = date,
                riddle = "What three positive numbers give the same result when multiplied and added together?",
                answer = "1, 2, and 3",
                explanation = "1 + 2 + 3 = 6 and 1 × 2 × 3 = 6!",
                emoji = "🤔"
            )
        )
        
        // Use date hash to pick consistent riddle for the day
        val index = date.hashCode().rem(riddles.size).let { if (it < 0) it + riddles.size else it }
        return riddles[index].copy(date = date)
    }
    
    /**
     * Generate random game of any type
     */
    fun generateRandomGame(difficulty: GameDifficulty): MathGame {
        return when (GameType.values().filter { it != GameType.MATH_RIDDLE }.random()) {
            GameType.NUMBER_PUZZLE -> generateNumberPuzzle(difficulty)
            GameType.PATTERN_MATCHING -> generatePatternMatching(difficulty)
            GameType.STORY_PROBLEM -> generateStoryProblem(difficulty)
            else -> generateNumberPuzzle(difficulty)
        }
    }
    
    /**
     * Helper to generate wrong answer choices
     */
    private fun generateWrongAnswers(correct: Int, count: Int): List<String> {
        val wrong = mutableSetOf<String>()
        while (wrong.size < count) {
            val offset = Random.nextInt(-5, 6)
            if (offset != 0) {
                val value = correct + offset
                if (value > 0) {
                    wrong.add(value.toString())
                }
            }
        }
        return wrong.toList()
    }
}
