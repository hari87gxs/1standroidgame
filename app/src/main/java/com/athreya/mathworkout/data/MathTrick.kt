package com.athreya.mathworkout.data

/**
 * Represents a math trick/shortcut that kids can learn
 */
data class MathTrick(
    val id: String,
    val name: String,
    val emoji: String,
    val shortDescription: String,
    val category: TrickCategory,
    val difficulty: TrickDifficulty,
    val steps: List<TrickStep>,
    val example: TrickExample,
    val tips: List<String>,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val practiceScore: Int = 0
)

/**
 * Category of math trick
 */
enum class TrickCategory {
    MULTIPLICATION,
    ADDITION,
    SUBTRACTION,
    DIVISION,
    MENTAL_MATH,
    NUMBER_PATTERNS
}

/**
 * Difficulty level for tricks
 */
enum class TrickDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * Step in explaining a trick
 */
data class TrickStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val visual: String? = null // Visual representation (could be text-based for now)
)

/**
 * Example demonstrating the trick
 */
data class TrickExample(
    val problem: String,
    val solution: String,
    val explanation: String
)

/**
 * Repository of all available math tricks
 */
object MathTricks {
    
    fun getAllTricks(): List<MathTrick> {
        return listOf(
            // Trick 1: Multiply by 9 using fingers
            MathTrick(
                id = "multiply_by_9",
                name = "Multiply by 9 with Your Fingers",
                emoji = "🖐️",
                shortDescription = "Use your fingers to multiply any number by 9",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Hold up your hands",
                        "Spread all 10 fingers in front of you"
                    ),
                    TrickStep(
                        2,
                        "Count to the number",
                        "If multiplying 9 × 7, count to finger #7 from the left"
                    ),
                    TrickStep(
                        3,
                        "Bend that finger down",
                        "Bend down the 7th finger"
                    ),
                    TrickStep(
                        4,
                        "Count fingers on each side",
                        "Left of bent finger = tens digit (6)\nRight of bent finger = ones digit (3)"
                    ),
                    TrickStep(
                        5,
                        "Read the answer",
                        "9 × 7 = 63!"
                    )
                ),
                example = TrickExample(
                    problem = "9 × 4 = ?",
                    solution = "36",
                    explanation = "Bend 4th finger: 3 fingers left (30) + 6 fingers right (6) = 36"
                ),
                tips = listOf(
                    "Works for 9 × 1 through 9 × 10",
                    "Left fingers = tens, right fingers = ones",
                    "Practice with different numbers!"
                )
            ),
            
            // Trick 2: Multiply by 11
            MathTrick(
                id = "multiply_by_11",
                name = "Multiply by 11 (Two-Digit Numbers)",
                emoji = "🎯",
                shortDescription = "Quick trick to multiply any 2-digit number by 11",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Take the two digits",
                        "Example: 23 × 11\nDigits are 2 and 3"
                    ),
                    TrickStep(
                        2,
                        "Add the digits together",
                        "2 + 3 = 5"
                    ),
                    TrickStep(
                        3,
                        "Put the sum in the middle",
                        "2 [5] 3 = 253"
                    ),
                    TrickStep(
                        4,
                        "That's your answer!",
                        "23 × 11 = 253"
                    )
                ),
                example = TrickExample(
                    problem = "45 × 11 = ?",
                    solution = "495",
                    explanation = "4 and 5 → add them (4+5=9) → put 9 in middle → 4[9]5 = 495"
                ),
                tips = listOf(
                    "If the sum is 10 or more, carry the 1",
                    "Example: 67 × 11 → 6+7=13 → 6[13]7 → 7[3]7 (carry 1) → 737",
                    "Works only for 2-digit numbers"
                )
            ),
            
            // Trick 3: Square numbers ending in 5
            MathTrick(
                id = "square_ending_5",
                name = "Square Numbers Ending in 5",
                emoji = "5️⃣",
                shortDescription = "Quickly square any number ending in 5",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.INTERMEDIATE,
                steps = listOf(
                    TrickStep(
                        1,
                        "Take the tens digit",
                        "For 25²: tens digit is 2"
                    ),
                    TrickStep(
                        2,
                        "Multiply by next number",
                        "2 × 3 = 6"
                    ),
                    TrickStep(
                        3,
                        "Add 25 at the end",
                        "625"
                    ),
                    TrickStep(
                        4,
                        "Done!",
                        "25² = 625"
                    )
                ),
                example = TrickExample(
                    problem = "75² = ?",
                    solution = "5625",
                    explanation = "7 × 8 = 56, add 25 → 5625"
                ),
                tips = listOf(
                    "Always ends in 25",
                    "Formula: n × (n+1), then add 25",
                    "Works for 15, 25, 35, 45, etc."
                )
            ),
            
            // Trick 4: Multiply by 5
            MathTrick(
                id = "multiply_by_5",
                name = "Multiply by 5 Quickly",
                emoji = "✖️",
                shortDescription = "Fast way to multiply any number by 5",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Multiply by 10",
                        "14 × 5 → First do 14 × 10 = 140"
                    ),
                    TrickStep(
                        2,
                        "Divide by 2",
                        "140 ÷ 2 = 70"
                    ),
                    TrickStep(
                        3,
                        "That's the answer!",
                        "14 × 5 = 70"
                    )
                ),
                example = TrickExample(
                    problem = "28 × 5 = ?",
                    solution = "140",
                    explanation = "28 × 10 = 280, then 280 ÷ 2 = 140"
                ),
                tips = listOf(
                    "Multiplying by 10 is easy (add a zero)",
                    "Dividing by 2 is halving",
                    "Works for any number!"
                )
            ),
            
            // Trick 5: Add numbers close to 100
            MathTrick(
                id = "add_near_100",
                name = "Add Numbers Close to 100",
                emoji = "💯",
                shortDescription = "Quick addition for numbers near 100",
                category = TrickCategory.ADDITION,
                difficulty = TrickDifficulty.INTERMEDIATE,
                steps = listOf(
                    TrickStep(
                        1,
                        "Find how far from 100",
                        "97 is 3 away from 100\n95 is 5 away from 100"
                    ),
                    TrickStep(
                        2,
                        "Add the differences",
                        "3 + 5 = 8"
                    ),
                    TrickStep(
                        3,
                        "Subtract from 200",
                        "200 - 8 = 192"
                    ),
                    TrickStep(
                        4,
                        "Done!",
                        "97 + 95 = 192"
                    )
                ),
                example = TrickExample(
                    problem = "98 + 96 = ?",
                    solution = "194",
                    explanation = "2 away + 4 away = 6 away. 200 - 6 = 194"
                ),
                tips = listOf(
                    "Works for numbers in the 90s",
                    "Remember: Start with 200",
                    "Faster than traditional adding"
                )
            ),
            
            // Trick 6: Subtract from 1000
            MathTrick(
                id = "subtract_from_1000",
                name = "Subtract from 1000",
                emoji = "➖",
                shortDescription = "Easy way to subtract any number from 1000",
                category = TrickCategory.SUBTRACTION,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Subtract each digit from 9",
                        "1000 - 456:\nSubtract 4 from 9 = 5\nSubtract 5 from 9 = 4"
                    ),
                    TrickStep(
                        2,
                        "Subtract last digit from 10",
                        "Subtract 6 from 10 = 4"
                    ),
                    TrickStep(
                        3,
                        "Write the results",
                        "544"
                    )
                ),
                example = TrickExample(
                    problem = "1000 - 237 = ?",
                    solution = "763",
                    explanation = "9-2=7, 9-3=6, 10-7=3 → 763"
                ),
                tips = listOf(
                    "Each digit from 9, except last from 10",
                    "No borrowing needed!",
                    "Super quick method"
                )
            ),
            
            // Trick 7: Check divisibility by 3
            MathTrick(
                id = "divisible_by_3",
                name = "Divisibility Rule for 3",
                emoji = "3️⃣",
                shortDescription = "Quickly check if a number divides by 3",
                category = TrickCategory.NUMBER_PATTERNS,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Add all digits",
                        "Example: 147\n1 + 4 + 7 = 12"
                    ),
                    TrickStep(
                        2,
                        "Check if sum divides by 3",
                        "12 ÷ 3 = 4 (yes!)"
                    ),
                    TrickStep(
                        3,
                        "Result",
                        "147 is divisible by 3"
                    )
                ),
                example = TrickExample(
                    problem = "Is 2,346 divisible by 3?",
                    solution = "Yes",
                    explanation = "2+3+4+6=15, and 15÷3=5, so yes!"
                ),
                tips = listOf(
                    "Works for any sized number",
                    "If digit sum is divisible by 3, the whole number is",
                    "Great for quick checking"
                )
            ),
            
            // Trick 8: Multiply by 4
            MathTrick(
                id = "multiply_by_4",
                name = "Multiply by 4 Fast",
                emoji = "4️⃣",
                shortDescription = "Double twice to multiply by 4",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.BEGINNER,
                steps = listOf(
                    TrickStep(
                        1,
                        "Double the number",
                        "23 × 4 → Double 23 = 46"
                    ),
                    TrickStep(
                        2,
                        "Double again",
                        "Double 46 = 92"
                    ),
                    TrickStep(
                        3,
                        "Done!",
                        "23 × 4 = 92"
                    )
                ),
                example = TrickExample(
                    problem = "17 × 4 = ?",
                    solution = "68",
                    explanation = "17 × 2 = 34, then 34 × 2 = 68"
                ),
                tips = listOf(
                    "Doubling is easier than multiplying",
                    "4 = 2 × 2",
                    "Works for any number"
                )
            ),
            
            // Trick 9: Multiply numbers close to 100
            MathTrick(
                id = "multiply_near_100",
                name = "Multiply Numbers Near 100",
                emoji = "💯",
                shortDescription = "Quick method for multiplying numbers close to 100",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.ADVANCED,
                steps = listOf(
                    TrickStep(
                        1,
                        "Find distance from 100",
                        "For 97 × 98: Both are 3 and 2 away from 100"
                    ),
                    TrickStep(
                        2,
                        "Subtract crosswise",
                        "97 - 2 = 95 (or 98 - 3 = 95). This is the first part",
                        visual = "97 × 98\n-3  -2\n95__ (first two digits)"
                    ),
                    TrickStep(
                        3,
                        "Multiply the differences",
                        "(-3) × (-2) = 6. This is the last part",
                        visual = "95__\n3 × 2 = 06"
                    ),
                    TrickStep(
                        4,
                        "Combine the parts",
                        "9506 is the answer!"
                    )
                ),
                example = TrickExample(
                    problem = "96 × 94 = ?",
                    solution = "9024",
                    explanation = "96 - 6 = 90 (or 94 - 4 = 90), then 4 × 6 = 24, so 9024"
                ),
                tips = listOf(
                    "Both numbers must be close to 100",
                    "If the last part is single digit, add a leading zero (6 becomes 06)",
                    "Practice with 95-99 range first"
                )
            ),
            
            // Trick 10: Square numbers ending in 1
            MathTrick(
                id = "square_ending_in_1",
                name = "Square Numbers Ending in 1",
                emoji = "1️⃣",
                shortDescription = "Fast technique to square numbers ending in 1",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.ADVANCED,
                steps = listOf(
                    TrickStep(
                        1,
                        "Remove the 1",
                        "For 31²: Work with 3 (the tens digit)"
                    ),
                    TrickStep(
                        2,
                        "Multiply by next number",
                        "3 × 4 = 12 (hundreds place)",
                        visual = "3 × (3+1) = 3 × 4 = 12"
                    ),
                    TrickStep(
                        3,
                        "Add middle digits",
                        "Add (2 × tens digit) for middle: 2 × 3 = 6"
                    ),
                    TrickStep(
                        4,
                        "End with 1",
                        "Answer: 12, then 6, then 1 = 961"
                    )
                ),
                example = TrickExample(
                    problem = "21² = ?",
                    solution = "441",
                    explanation = "2 × 3 = 6 (hundreds), 2 × 2 = 4 (tens), 1 (ones) = 441"
                ),
                tips = listOf(
                    "Pattern: n1² always ends in 1",
                    "The middle digit is always 2 × tens digit",
                    "Works for 11, 21, 31, 41, etc."
                )
            ),
            
            // Trick 11: Vedic Math - Multiply by 12
            MathTrick(
                id = "multiply_by_12",
                name = "Multiply by 12 (Vedic Method)",
                emoji = "🧮",
                shortDescription = "Ancient Vedic technique for multiplying by 12",
                category = TrickCategory.MULTIPLICATION,
                difficulty = TrickDifficulty.ADVANCED,
                steps = listOf(
                    TrickStep(
                        1,
                        "Double the number",
                        "For 23 × 12: Double 23 = 46"
                    ),
                    TrickStep(
                        2,
                        "Multiply original by 10",
                        "23 × 10 = 230"
                    ),
                    TrickStep(
                        3,
                        "Add them together",
                        "230 + 46 = 276"
                    )
                ),
                example = TrickExample(
                    problem = "34 × 12 = ?",
                    solution = "408",
                    explanation = "(34 × 2) + (34 × 10) = 68 + 340 = 408"
                ),
                tips = listOf(
                    "12 = 10 + 2, so split the multiplication",
                    "Mental math becomes easier by breaking it down",
                    "Same pattern works for 13, 14, etc."
                )
            )
        )
    }
    
    fun getTrickById(id: String): MathTrick? {
        return getAllTricks().find { it.id == id }
    }
    
    fun getTricksByCategory(category: TrickCategory): List<MathTrick> {
        return getAllTricks().filter { it.category == category }
    }
    
    fun getTricksByDifficulty(difficulty: TrickDifficulty): List<MathTrick> {
        return getAllTricks().filter { it.difficulty == difficulty }
    }
}
