package com.athreya.mathworkout.data.avatar

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Avatar entity - Famous mathematicians that kids can unlock with XP
 * Each avatar includes educational trivia about the mathematician
 */
@Entity(tableName = "avatars")
data class Avatar(
    @PrimaryKey val avatarId: String,
    val name: String,               // Mathematician's name
    val imageUrl: String,           // URL or resource ID for portrait
    val emoji: String,              // Emoji representation for fallback
    val era: String,                // Time period (e.g., "Ancient Greece", "1700s")
    val category: AvatarCategory,   // Field of mathematics
    val xpCost: Int,               // XP points needed to unlock
    val rarity: AvatarRarity,      // Common, Rare, Epic, Legendary
    val trivia: String,            // Fun fact about the mathematician
    val contribution: String,       // Their major contribution to mathematics
    val funFact: String,           // Additional interesting fact
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

/**
 * Avatar categories for different fields of mathematics
 */
enum class AvatarCategory {
    ARITHMETIC,     // Basic arithmetic pioneers
    GEOMETRY,       // Geometry and shapes
    ALGEBRA,        // Algebra and equations
    CALCULUS,       // Calculus and analysis
    NUMBER_THEORY,  // Number theory and primes
    STATISTICS,     // Statistics and probability
    COMPUTER_SCIENCE, // Computing and algorithms
    LOGIC,          // Logic and set theory
    APPLIED_MATH,   // Applied mathematics
    MODERN_MATH     // Contemporary mathematics
}

/**
 * Avatar rarity levels
 */
enum class AvatarRarity(val displayName: String, val color: Long) {
    COMMON("Common", 0xFF9E9E9E),       // Gray - Ancient mathematicians
    RARE("Rare", 0xFF4CAF50),           // Green - Medieval/Renaissance  
    EPIC("Epic", 0xFF9C27B0),           // Purple - Enlightenment era
    LEGENDARY("Legendary", 0xFFFF9800)   // Orange/Gold - Modern pioneers
}

/**
 * Predefined collection of famous mathematicians
 */
object AvatarCollection {
    
    fun getAllAvatars(): List<Avatar> = listOf(
        // DEFAULT - Always unlocked
        Avatar(
            avatarId = "default",
            name = "Math Explorer",
            imageUrl = "default_avatar",
            emoji = "🧮",
            era = "Present",
            category = AvatarCategory.ARITHMETIC,
            xpCost = 0,
            rarity = AvatarRarity.COMMON,
            trivia = "Every mathematician started as a beginner!",
            contribution = "You're starting your mathematical journey",
            funFact = "The best time to start learning is now!",
            isUnlocked = true
        ),
        
        // COMMON (50-100 XP) - Ancient Mathematicians
        Avatar(
            avatarId = "pythagoras",
            name = "Pythagoras",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Kapitolinischer_Pythagoras_adjusted.jpg/440px-Kapitolinischer_Pythagoras_adjusted.jpg",
            emoji = "📐",
            era = "Ancient Greece (570-495 BC)",
            category = AvatarCategory.GEOMETRY,
            xpCost = 50,
            rarity = AvatarRarity.COMMON,
            trivia = "Founded a secret society that studied mathematics and philosophy!",
            contribution = "Pythagorean Theorem: a² + b² = c²",
            funFact = "His followers believed numbers were sacred and had mystical powers"
        ),
        
        Avatar(
            avatarId = "euclid",
            name = "Euclid",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Euklid-von-Alexandria_1.jpg/440px-Euklid-von-Alexandria_1.jpg",
            emoji = "📏",
            era = "Ancient Greece (300 BC)",
            category = AvatarCategory.GEOMETRY,
            xpCost = 75,
            rarity = AvatarRarity.COMMON,
            trivia = "His book 'Elements' was used as a textbook for 2,000 years!",
            contribution = "Father of Geometry - wrote the most influential math textbook ever",
            funFact = "We know almost nothing about his personal life, only his amazing math"
        ),
        
        Avatar(
            avatarId = "archimedes",
            name = "Archimedes",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Domenico-Fetti_Archimedes_1620.jpg/440px-Domenico-Fetti_Archimedes_1620.jpg",
            emoji = "⚗️",
            era = "Ancient Greece (287-212 BC)",
            category = AvatarCategory.APPLIED_MATH,
            xpCost = 100,
            rarity = AvatarRarity.COMMON,
            trivia = "Shouted 'Eureka!' and ran naked through the streets after a discovery!",
            contribution = "Calculated π, invented war machines, discovered buoyancy",
            funFact = "He was so focused on math that he didn't notice his city being invaded"
        ),
        
        Avatar(
            avatarId = "hypatia",
            name = "Hypatia",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Hypatia_portrait.png/440px-Hypatia_portrait.png",
            emoji = "👩‍🏫",
            era = "Ancient Egypt (355-415 AD)",
            category = AvatarCategory.APPLIED_MATH,
            xpCost = 100,
            rarity = AvatarRarity.COMMON,
            trivia = "First famous female mathematician in history!",
            contribution = "Taught mathematics, astronomy, and philosophy in Alexandria",
            funFact = "Students traveled from all over the world to learn from her"
        ),
        
        // RARE (150-300 XP) - Medieval & Renaissance
        Avatar(
            avatarId = "al_khwarizmi",
            name = "Al-Khwarizmi",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Al-Khwarizmi_portrait.jpg/440px-Al-Khwarizmi_portrait.jpg",
            emoji = "🔢",
            era = "Islamic Golden Age (780-850 AD)",
            category = AvatarCategory.ALGEBRA,
            xpCost = 150,
            rarity = AvatarRarity.RARE,
            trivia = "The word 'algorithm' comes from his name!",
            contribution = "Father of Algebra - wrote the first book on solving equations",
            funFact = "Also introduced Arabic numerals (0-9) to Europe"
        ),
        
        Avatar(
            avatarId = "fibonacci",
            name = "Fibonacci",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Fibonacci2.jpg/440px-Fibonacci2.jpg",
            emoji = "🌻",
            era = "Medieval Italy (1170-1250)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 200,
            rarity = AvatarRarity.RARE,
            trivia = "Discovered a sequence where each number is the sum of the previous two!",
            contribution = "Fibonacci Sequence: 1, 1, 2, 3, 5, 8, 13, 21...",
            funFact = "This pattern appears in pinecones, flowers, and even galaxies!"
        ),
        
        Avatar(
            avatarId = "descartes",
            name = "René Descartes",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/Frans_Hals_-_Portret_van_Ren%C3%A9_Descartes.jpg/440px-Frans_Hals_-_Portret_van_Ren%C3%A9_Descartes.jpg",
            emoji = "📊",
            era = "France (1596-1650)",
            category = AvatarCategory.GEOMETRY,
            xpCost = 250,
            rarity = AvatarRarity.RARE,
            trivia = "Created the coordinate system (x, y) while lying in bed watching a fly!",
            contribution = "Connected algebra and geometry, said 'I think, therefore I am'",
            funFact = "Slept until 11 AM every day because he believed in morning rest"
        ),
        
        Avatar(
            avatarId = "fermat",
            name = "Pierre de Fermat",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Pierre_de_Fermat.jpg/440px-Pierre_de_Fermat.jpg",
            emoji = "🔐",
            era = "France (1607-1665)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 300,
            rarity = AvatarRarity.RARE,
            trivia = "Left a mystery unsolved for 358 years!",
            contribution = "Fermat's Last Theorem - finally proved in 1995",
            funFact = "He wrote in margins: 'I have a proof, but it's too large to fit here'"
        ),
        
        // EPIC (400-700 XP) - Enlightenment Era
        Avatar(
            avatarId = "newton",
            name = "Isaac Newton",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/GodfreyKneller-IsaacNewton-1689.jpg/440px-GodfreyKneller-IsaacNewton-1689.jpg",
            emoji = "🍎",
            era = "England (1642-1727)",
            category = AvatarCategory.CALCULUS,
            xpCost = 400,
            rarity = AvatarRarity.EPIC,
            trivia = "Invented calculus during a pandemic when Cambridge University closed!",
            contribution = "Laws of motion, gravity, calculus - changed science forever",
            funFact = "Argued for decades about who invented calculus first"
        ),
        
        Avatar(
            avatarId = "euler",
            name = "Leonhard Euler",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Leonhard_Euler.jpg/440px-Leonhard_Euler.jpg",
            emoji = "∞",
            era = "Switzerland (1707-1783)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 450,
            rarity = AvatarRarity.EPIC,
            trivia = "Most productive mathematician ever - wrote half his papers while blind!",
            contribution = "Contributed to every field of math, created e (2.71828...)",
            funFact = "Wrote 886 books and papers - more than anyone in history"
        ),
        
        Avatar(
            avatarId = "gauss",
            name = "Carl Friedrich Gauss",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Carl_Friedrich_Gauss.jpg/440px-Carl_Friedrich_Gauss.jpg",
            emoji = "👑",
            era = "Germany (1777-1855)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 500,
            rarity = AvatarRarity.EPIC,
            trivia = "At age 3, he corrected his father's math mistake!",
            contribution = "Number theory, statistics, astronomy - called 'Prince of Mathematics'",
            funFact = "At age 10, added numbers 1-100 instantly using a clever trick"
        ),
        
        Avatar(
            avatarId = "ada_lovelace",
            name = "Ada Lovelace",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/Ada_Lovelace_portrait.jpg/440px-Ada_Lovelace_portrait.jpg",
            emoji = "💻",
            era = "England (1815-1852)",
            category = AvatarCategory.COMPUTER_SCIENCE,
            xpCost = 550,
            rarity = AvatarRarity.EPIC,
            trivia = "World's first computer programmer - 100 years before computers existed!",
            contribution = "Wrote the first algorithm meant to be processed by a machine",
            funFact = "Daughter of poet Lord Byron, but chose math over poetry"
        ),
        
        Avatar(
            avatarId = "ramanujan",
            name = "Srinivasa Ramanujan",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Srinivasa_Ramanujan_-_OPC_-_1.jpg/440px-Srinivasa_Ramanujan_-_OPC_-_1.jpg",
            emoji = "🧠",
            era = "India (1887-1920)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 600,
            rarity = AvatarRarity.EPIC,
            trivia = "Taught himself advanced math with no formal training!",
            contribution = "Discovered 3,900 results, many still being explored today",
            funFact = "Said his mathematical visions came from a goddess in his dreams"
        ),
        
        Avatar(
            avatarId = "emmy_noether",
            name = "Emmy Noether",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Noether.jpg/440px-Noether.jpg",
            emoji = "🔬",
            era = "Germany (1882-1935)",
            category = AvatarCategory.ALGEBRA,
            xpCost = 650,
            rarity = AvatarRarity.EPIC,
            trivia = "Einstein called her the most important woman in mathematics!",
            contribution = "Abstract algebra, Noether's Theorem in physics",
            funFact = "Wasn't paid for years because universities wouldn't hire women"
        ),
        
        Avatar(
            avatarId = "turing",
            name = "Alan Turing",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Alan_Turing_Aged_16.jpg/440px-Alan_Turing_Aged_16.jpg",
            emoji = "🤖",
            era = "England (1912-1954)",
            category = AvatarCategory.COMPUTER_SCIENCE,
            xpCost = 700,
            rarity = AvatarRarity.EPIC,
            trivia = "Helped win WW2 by cracking secret Nazi codes!",
            contribution = "Father of computer science and artificial intelligence",
            funFact = "The 'Turing Test' checks if AI can think like a human"
        ),
        
        // LEGENDARY (800-1500 XP) - Modern Pioneers
        Avatar(
            avatarId = "cantor",
            name = "Georg Cantor",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Georg_Cantor2.jpg/440px-Georg_Cantor2.jpg",
            emoji = "∞",
            era = "Germany (1845-1918)",
            category = AvatarCategory.LOGIC,
            xpCost = 800,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Proved that some infinities are bigger than others!",
            contribution = "Created set theory and studied infinite numbers",
            funFact = "His ideas were so revolutionary that other mathematicians attacked him"
        ),
        
        Avatar(
            avatarId = "godel",
            name = "Kurt Gödel",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/1925_kurt_g%C3%B6del.png/440px-1925_kurt_g%C3%B6del.png",
            emoji = "🎓",
            era = "Austria (1906-1978)",
            category = AvatarCategory.LOGIC,
            xpCost = 900,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Proved there are true things that can never be proven!",
            contribution = "Incompleteness Theorems - changed our understanding of math itself",
            funFact = "Einstein walked to work with him just to enjoy their conversations"
        ),
        
        Avatar(
            avatarId = "von_neumann",
            name = "John von Neumann",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/JohnvonNeumann-LosAlamos.gif/440px-JohnvonNeumann-LosAlamos.gif",
            emoji = "⚛️",
            era = "Hungary/USA (1903-1957)",
            category = AvatarCategory.COMPUTER_SCIENCE,
            xpCost = 1000,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Could multiply 8-digit numbers in his head instantly!",
            contribution = "Game theory, computer architecture, quantum mechanics",
            funFact = "His computer design is still used in almost every computer today"
        ),
        
        Avatar(
            avatarId = "erdos",
            name = "Paul Erdős",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Paul_Erd%C5%91s_1992.jpg/440px-Paul_Erd%C5%91s_1992.jpg",
            emoji = "🌍",
            era = "Hungary (1913-1996)",
            category = AvatarCategory.NUMBER_THEORY,
            xpCost = 1100,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Wrote 1,500 papers with 511 different co-authors!",
            contribution = "Combinatorics, graph theory, number theory",
            funFact = "Lived from a suitcase, traveling the world to do math with friends"
        ),
        
        Avatar(
            avatarId = "nash",
            name = "John Nash",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/John_Forbes_Nash%2C_Jr._at_the_40th_anniversary_celebration_of_the_Nash_embedding_theorem.jpg/440px-John_Forbes_Nash%2C_Jr._at_the_40th_anniversary_celebration_of_the_Nash_embedding_theorem.jpg",
            emoji = "🏆",
            era = "USA (1928-2015)",
            category = AvatarCategory.APPLIED_MATH,
            xpCost = 1200,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Won the Nobel Prize in Economics for his math discoveries!",
            contribution = "Nash Equilibrium - used in economics, politics, and biology",
            funFact = "Subject of the movie 'A Beautiful Mind', overcame mental illness"
        ),
        
        Avatar(
            avatarId = "grothendieck",
            name = "Alexander Grothendieck",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Alexander_Grothendieck.jpg/440px-Alexander_Grothendieck.jpg",
            emoji = "🎨",
            era = "Germany/France (1928-2014)",
            category = AvatarCategory.ALGEBRA,
            xpCost = 1300,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Rewrote entire fields of mathematics in revolutionary ways!",
            contribution = "Algebraic geometry, category theory",
            funFact = "Later gave up math to become a recluse and environmental activist"
        ),
        
        Avatar(
            avatarId = "katherine_johnson",
            name = "Katherine Johnson",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d5/Katherine_Johnson_in_2008.jpg/440px-Katherine_Johnson_in_2008.jpg",
            emoji = "🚀",
            era = "USA (1918-2020)",
            category = AvatarCategory.APPLIED_MATH,
            xpCost = 1400,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Her calculations sent astronauts to the moon!",
            contribution = "Computed flight paths for NASA, including Apollo 11",
            funFact = "Astronaut John Glenn insisted she personally check his flight numbers"
        ),
        
        Avatar(
            avatarId = "tao",
            name = "Terence Tao",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Terence_Tao%2C_Royal_Society_%28crop%29.jpg/440px-Terence_Tao%2C_Royal_Society_%28crop%29.jpg",
            emoji = "⭐",
            era = "Australia (1975-present)",
            category = AvatarCategory.MODERN_MATH,
            xpCost = 1500,
            rarity = AvatarRarity.LEGENDARY,
            trivia = "Child prodigy who attended university at age 9!",
            contribution = "Number theory, analysis, partial differential equations",
            funFact = "Youngest ever Fields Medal winner at 31, still actively researching"
        )
    )
}
