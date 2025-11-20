# Athreya's Math Workout - Complete Code Explanation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Project Structure](#project-structure)
4. [Layer-by-Layer Explanation](#layer-by-layer-explanation)
5. [Core Systems Deep Dive](#core-systems-deep-dive)
6. [UI Components Explained](#ui-components-explained)
7. [Data Flow Examples](#data-flow-examples)
8. [State Management Patterns](#state-management-patterns)
9. [Navigation System](#navigation-system)
10. [Firebase Integration](#firebase-integration)
11. [Best Practices & Patterns](#best-practices--patterns)

---

## Project Overview

**Athreya's Math Workout** is a comprehensive Android math education app built with modern Android development practices. The app combines gamification, social features, and educational content to create an engaging learning experience.

### Key Technologies
- **Language**: Kotlin 1.9.0
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (local) + Firebase Firestore (cloud)
- **Authentication**: Firebase Auth
- **Navigation**: Compose Navigation
- **Async**: Kotlin Coroutines + Flow
- **Material Design**: Material 3 (Material You)

### App Features Summary
- **6 Game Modes**: Addition, Subtraction, Multiplication, Division, Mixed, Brain Teasers
- **Interactive Games**: Sudoku, Math Tricks Library, Daily Riddles
- **50+ Badges**: Achievement system with 5 categories and 5 rarity levels
- **Social**: Groups, Player Challenges, Global Leaderboard
- **Educational**: Smart hints, educational explanations, famous mathematicians
- **Customization**: 10+ themes, difficulty levels, time modes
- **Gamification**: Daily challenges, streaks, rank progression

---

## Architecture & Design Patterns

### MVVM Architecture

The app follows the **Model-View-ViewModel** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│                   VIEW LAYER                        │
│  (Jetpack Compose UI - ui/screens/, ui/components/)│
│                                                      │
│  Responsibilities:                                   │
│  • Render UI based on state                         │
│  • Handle user interactions                         │
│  • Emit events to ViewModel                         │
│  • Display data from StateFlow                      │
└───────────────┬─────────────────────────────────────┘
                │ Observes StateFlow
                │ Emits user events
┌───────────────▼─────────────────────────────────────┐
│                VIEWMODEL LAYER                      │
│            (viewmodel/XyzViewModel.kt)              │
│                                                      │
│  Responsibilities:                                   │
│  • Hold UI state (StateFlow)                        │
│  • Handle business logic                            │
│  • Coordinate repository calls                      │
│  • Transform data for UI                            │
│  • Manage lifecycle-aware operations                │
└───────────────┬─────────────────────────────────────┘
                │ Calls repositories
                │ Receives data
┌───────────────▼─────────────────────────────────────┐
│               REPOSITORY LAYER                      │
│         (data/repository/XyzRepository.kt)          │
│                                                      │
│  Responsibilities:                                   │
│  • Abstract data sources                            │
│  • Coordinate local + remote data                   │
│  • Implement caching strategies                     │
│  • Handle data transformations                      │
│  • Error handling and retries                       │
└───────────────┬─────────────────────────────────────┘
                │ Queries/Updates
                │
        ┌───────┴───────┐
        │               │
┌───────▼──────┐ ┌──────▼────────┐
│  LOCAL DATA  │ │  REMOTE DATA  │
│  (Room DB)   │ │  (Firebase)   │
│              │ │               │
│ • DAOs       │ │ • Firestore   │
│ • Entities   │ │ • Auth        │
│ • Prefs      │ │ • FCM         │
└──────────────┘ └───────────────┘
```

### Key Design Patterns

1. **Repository Pattern**: Abstract data access, allowing easy swapping of data sources
2. **Observer Pattern**: ViewModels expose `StateFlow`, UI observes and reacts
3. **Factory Pattern**: `ViewModelFactory` creates ViewModels with dependencies
4. **Singleton Pattern**: Managers (BadgeManager, SettingsManager) ensure single instances
5. **State Pattern**: Sealed classes represent different UI states (Loading, Success, Error)
6. **Strategy Pattern**: Different game types implement common interfaces

---

## Project Structure

```
app/src/main/java/com/athreya/mathworkout/
│
├── MainActivity.kt                      # App entry point, theme setup
│
├── data/                                # Data layer - Models, Managers, Repositories
│   ├── Achievement.kt                   # Achievement data model & definitions
│   ├── AchievementManager.kt            # Achievement unlock logic & tracking
│   ├── Badge.kt                         # Badge data model (50+ badges)
│   ├── BadgeManager.kt                  # Badge unlock & progress tracking
│   ├── Challenge.kt                     # Player challenge data model
│   ├── ChallengeNotification.kt         # Challenge notification data
│   ├── DailyChallenge.kt                # Daily challenge model & generation
│   ├── Group.kt                         # Group data model
│   ├── HighScore.kt                     # High score entity (Room)
│   ├── MathTrick.kt                     # Math trick learning content
│   ├── Mathematician.kt                 # Famous mathematician profiles
│   ├── Rank.kt                          # Player rank system (Beginner → Grandmaster)
│   ├── Riddle.kt                        # Daily riddle model
│   ├── SettingsManager.kt               # Game settings & preferences
│   ├── ThemePreferencesManager.kt       # Theme unlock & selection
│   ├── UserPreferencesManager.kt        # User data persistence
│   ├── dao/
│   │   ├── HighScoreDao.kt              # Room DAO for high scores
│   │   ├── GroupDao.kt                  # Room DAO for groups
│   │   └── ChallengeDao.kt              # Room DAO for challenges
│   ├── database/
│   │   └── AppDatabase.kt               # Room database configuration
│   └── repository/
│       ├── ScoreRepository.kt           # Score data coordination (local + Firebase)
│       ├── GroupRepository.kt           # Group data management
│       └── ChallengeRepository.kt       # Challenge data management
│
├── game/                                # Game logic - Question generation, Sudoku engine
│   ├── QuestionGenerator.kt             # Generate math questions with hints
│   ├── SudokuEngine.kt                  # Sudoku puzzle generation & validation
│   └── GameScorer.kt                    # Score calculation logic
│
├── ui/                                  # UI layer - All composables
│   ├── components/                      # Reusable UI components
│   │   ├── BadgeDisplay.kt              # Badge UI (Card, Grid, Row, Indicator)
│   │   ├── DailyBonusDialog.kt          # Daily login bonus popup
│   │   ├── DifficultySelector.kt        # Difficulty selection UI
│   │   ├── GameCard.kt                  # Game mode selection cards
│   │   ├── GlobalLeaderboardScreen.kt   # Global rankings display
│   │   ├── HighScoreCard.kt             # High score list item
│   │   ├── LeaderboardCard.kt           # Leaderboard entry card
│   │   ├── QuestionCard.kt              # Question display with options
│   │   ├── RankCard.kt                  # Rank display card
│   │   └── ScoreGraph.kt                # Score history graph
│   │
│   └── screens/                         # Full screen composables
│       ├── AchievementsScreen.kt        # Achievement listing & details
│       ├── BadgesScreen.kt              # Badge collection with categories
│       ├── ChallengesScreen.kt          # Player challenges (pending, active, completed)
│       ├── DailyRiddleScreen.kt         # Daily riddle puzzle
│       ├── GameScreen.kt                # Main gameplay screen with hints
│       ├── GroupDetailScreen.kt         # Group leaderboard & management
│       ├── GroupsScreen.kt              # Groups list & creation
│       ├── HomeScreen.kt                # Main home screen with badges
│       ├── MathematiciansScreen.kt      # Famous mathematicians profiles
│       ├── MathTricksScreen.kt          # Math tricks library & practice
│       ├── PrivacyPolicyScreen.kt       # Privacy policy viewer
│       ├── RegisterScreen.kt            # User registration for global features
│       ├── SettingsScreen.kt            # Settings with "How to Play" guide
│       ├── SudokuScreen.kt              # Sudoku game with timer
│       └── ThemesScreen.kt              # Theme selection & preview
│
├── viewmodel/                           # ViewModels - Business logic & state management
│   ├── AchievementViewModel.kt          # Achievement state & unlocking
│   ├── BadgeViewModel.kt                # Badge filtering & progress
│   ├── ChallengeViewModel.kt            # Challenge creation & management
│   ├── DailyRiddleViewModel.kt          # Riddle state & answer checking
│   ├── GameViewModel.kt                 # Game state, hints, scoring
│   ├── GroupViewModel.kt                # Group operations & leaderboards
│   ├── HomeViewModel.kt                 # Home screen state with badges
│   ├── LeaderboardViewModel.kt          # Global leaderboard data
│   ├── MathTricksViewModel.kt           # Math tricks content & practice
│   ├── RegisterViewModel.kt             # Registration flow
│   ├── SettingsViewModel.kt             # Settings state
│   ├── SudokuViewModel.kt               # Sudoku game state & validation
│   └── ThemeViewModel.kt                # Theme management
│
├── theme/                               # Material Theme definitions
│   ├── Color.kt                         # Color palette definitions
│   ├── Theme.kt                         # Material 3 theme setup
│   ├── Type.kt                          # Typography definitions
│   └── themes/                          # Custom theme configurations
│       ├── DefaultTheme.kt              # Default theme colors
│       ├── NeonTheme.kt                 # Neon theme (unlockable)
│       ├── ForestTheme.kt               # Forest theme (unlockable)
│       └── ...                          # Other custom themes
│
└── utils/                               # Utility functions & extensions
    ├── DateUtils.kt                     # Date formatting & calculations
    ├── StringUtils.kt                   # String manipulation helpers
    └── ComposableUtils.kt               # Compose helper functions
```

---

## Layer-by-Layer Explanation

### 1. Data Layer (data/)

The data layer handles all data operations, from database queries to Firebase sync.

#### **Badge System**

**Badge.kt** - Badge data model with 50+ unique badges

```kotlin
// Badge Categories
enum class BadgeCategory {
    SPEED,      // Time-based achievements (e.g., complete in 60 seconds)
    ACCURACY,   // Precision-based (e.g., 100% accuracy 5 times)
    COLLECTION, // Completionist (e.g., try all game modes)
    CHALLENGE,  // Social/competitive (e.g., win 50 challenges)
    DEDICATION  // Consistency (e.g., 30-day streak)
}

// Badge Rarities (affects UI color and prestige)
enum class BadgeRarity(val displayName: String, val color: Color) {
    BRONZE("Bronze", Color(0xFFCD7F32)),    // Common achievements
    SILVER("Silver", Color(0xFFC0C0C0)),    // Intermediate
    GOLD("Gold", Color(0xFFFFD700)),        // Advanced
    PLATINUM("Platinum", Color(0xFFE5E4E2)), // Expert
    DIAMOND("Diamond", Color(0xFFB9F2FF))   // Legendary
}

// Example badge definition
val SPEED_DEMON = Badge(
    id = "speed_demon",                     // Unique identifier
    name = "Speed Demon",                   // Display name
    description = "Complete 10 games in Quick mode", // What to do
    category = BadgeCategory.SPEED,         // Category for filtering
    rarity = BadgeRarity.BRONZE,           // Difficulty/prestige
    icon = "⚡",                            // Emoji icon
    requirement = "10 quick games"          // Human-readable requirement
)
```

**Why this design?**
- **Enums for categories/rarities**: Type-safe, easy to iterate for UI tabs
- **Emoji icons**: Universal, colorful, no image assets needed
- **Color in rarity**: Direct UI integration without mapping
- **Flat structure**: No inheritance, easy to serialize for Firebase

**BadgeManager.kt** - Badge unlock logic & progress tracking

```kotlin
class BadgeManager(private val userPreferences: UserPreferencesManager) {
    
    // Get user's unlocked badges (for display)
    fun getUnlockedBadges(): List<Badge> {
        val unlockedIds = userPreferences.getUnlockedBadgeIds()
        return Badges.getAllBadges().filter { it.id in unlockedIds }
    }
    
    // Update badge progress after game completion
    fun updateBadgeProgress(gameStats: GameStats): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        
        // Check all badges
        Badges.getAllBadges().forEach { badge ->
            if (!isBadgeUnlocked(badge.id)) {
                if (checkBadgeCondition(badge, gameStats)) {
                    unlockBadge(badge.id)
                    newlyUnlocked.add(badge)
                }
            }
        }
        
        return newlyUnlocked // Return new badges for celebration UI
    }
    
    // Badge unlock conditions
    private fun checkBadgeCondition(badge: Badge, stats: GameStats): Boolean {
        return when (badge.id) {
            "speed_demon" -> stats.quickGamesPlayed >= 10
            "sharpshooter" -> stats.correctStreakCurrent >= 10
            "perfectionist" -> stats.perfectGamesCount >= 5
            "daily_warrior" -> stats.dailyStreak >= 7
            "month_master" -> stats.dailyStreak >= 30
            "challenger" -> stats.challengeWins >= 1
            "champion" -> stats.challengeWins >= 50
            // ... all 50+ badge conditions
            else -> false
        }
    }
}
```

**Why this approach?**
- **Centralized logic**: All badge conditions in one place
- **Returns new unlocks**: Allows ViewModel to show celebration
- **Stats-based checks**: Pure functions, easy to test
- **When expression**: Exhaustive, compiler ensures all badges checked

#### **Achievement System**

**AchievementManager.kt** - Achievement tracking with theme unlocks

```kotlin
class AchievementManager(private val context: Context) {
    
    // Reactive state for UI
    private val _newlyUnlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlockedAchievements: StateFlow<List<Achievement>> = 
        _newlyUnlockedAchievements.asStateFlow()
    
    // Track game completion and check achievements
    fun trackGameCompletion(
        score: Int,
        totalQuestions: Int,
        correctAnswers: Int,
        timeMultiplier: Float
    ) {
        // Update statistics
        totalGamesPlayed++
        
        if (correctAnswers == totalQuestions) {
            perfectGamesCount++
        }
        
        if (timeMultiplier >= 3.0f) {
            speedMultiplier3xCount++
        }
        
        if (score > highScoreSingleGame) {
            highScoreSingleGame = score
        }
        
        totalPointsAccumulated += score
        
        // Check for new achievements
        checkAndUnlockAchievements()
    }
    
    private fun checkAndUnlockAchievements() {
        val newlyUnlocked = mutableListOf<Achievement>()
        
        Achievement.getAllAchievements().forEach { achievement ->
            if (!isAchievementUnlocked(achievement.id)) {
                val isUnlocked = when (achievement.id) {
                    "quick_learner" -> totalGamesPlayed >= 1
                    "speed_demon" -> speedMultiplier3xCount >= 30
                    "perfect_score" -> perfectGamesCount >= 5
                    "streak_master" -> getCurrentStreak() >= 7
                    "veteran_player" -> totalGamesPlayed >= 50
                    "high_scorer" -> highScoreSingleGame >= 10000
                    "point_collector" -> totalPointsAccumulated >= 50000
                    else -> false
                }
                
                if (isUnlocked) {
                    unlockAchievement(achievement.id)
                    newlyUnlocked.add(achievement.copy(isUnlocked = true))
                    
                    // Unlock associated theme
                    achievement.unlockedThemeId?.let { themeId ->
                        ThemePreferencesManager(context).unlockTheme(themeId)
                    }
                }
            }
        }
        
        // Emit to UI for celebration
        if (newlyUnlocked.isNotEmpty()) {
            _newlyUnlockedAchievements.value = newlyUnlocked
        }
    }
    
    // Get current rank based on total points
    fun getCurrentRank(): Rank {
        return Rank.getRankForPoints(getTotalPoints())
    }
}
```

**Key patterns:**
- **StateFlow for UI updates**: UI can observe new achievements reactively
- **Theme unlocking**: Achievements can unlock premium themes
- **Stats aggregation**: All game stats tracked in one place
- **Rank calculation**: Rank determined by total accumulated points

#### **Room Database**

**AppDatabase.kt** - Room database configuration

```kotlin
@Database(
    entities = [
        HighScore::class,
        Group::class,
        Challenge::class
    ],
    version = 9, // Incremented with schema changes
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun groupDao(): GroupDao
    abstract fun challengeDao(): ChallengeDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "math_workout_database"
                )
                .fallbackToDestructiveMigration() // For development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**HighScore.kt** - Room entity for high scores

```kotlin
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    
    val playerName: String,
    val score: Int,
    val gameType: String,
    val difficulty: Int,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Cloud sync
    val firebaseId: String? = null,  // Firebase document ID
    val synced: Boolean = false       // Whether synced to cloud
)
```

**HighScoreDao.kt** - Data access object

```kotlin
@Dao
interface HighScoreDao {
    
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<HighScore>>
    
    @Query("SELECT * FROM high_scores WHERE gameType = :gameType ORDER BY score DESC LIMIT 10")
    fun getTopScoresByType(gameType: String): Flow<List<HighScore>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highScore: HighScore)
    
    @Query("SELECT * FROM high_scores WHERE synced = 0")
    suspend fun getUnsyncedScores(): List<HighScore>
    
    @Update
    suspend fun update(highScore: HighScore)
}
```

**Why Flow?**
- `Flow<List<HighScore>>` is **reactive**: UI automatically updates when database changes
- Room automatically triggers Flow emission on updates
- No manual refresh logic needed

#### **Repositories**

**ScoreRepository.kt** - Coordinate local + cloud data

```kotlin
class ScoreRepository(
    private val highScoreDao: HighScoreDao,
    private val firebaseService: FirebaseScoreService
) {
    
    // Get high scores (local first, sync in background)
    fun getHighScores(): Flow<List<HighScore>> {
        // Start background sync
        syncScoresFromFirebase()
        
        // Return local data immediately
        return highScoreDao.getTopScores()
    }
    
    // Save score locally and sync to Firebase
    suspend fun saveScore(highScore: HighScore) {
        // Save locally first (fast, always succeeds)
        val localScore = highScore.copy(synced = false)
        highScoreDao.insert(localScore)
        
        // Sync to Firebase in background
        try {
            val firebaseId = firebaseService.uploadScore(localScore)
            
            // Update local record with Firebase ID
            highScoreDao.update(
                localScore.copy(
                    firebaseId = firebaseId,
                    synced = true
                )
            )
        } catch (e: Exception) {
            // Sync failed, but local save succeeded
            // Will retry on next sync
        }
    }
    
    // Background sync from Firebase to local
    private fun syncScoresFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteScores = firebaseService.fetchScores()
                remoteScores.forEach { score ->
                    highScoreDao.insert(score.copy(synced = true))
                }
            } catch (e: Exception) {
                // Sync failed, local data still available
            }
        }
    }
}
```

**Repository benefits:**
- **Offline-first**: Local data shown immediately, syncs when possible
- **Error isolation**: Network errors don't crash app
- **Single source of truth**: ViewModel doesn't care about data source
- **Background sync**: Non-blocking, improves UX

---

### 2. Game Layer (game/)

Game logic separate from UI for testability.

#### **QuestionGenerator.kt** - Generate questions with educational content

```kotlin
object QuestionGenerator {
    
    fun generateQuestion(gameType: GameType, difficulty: Int): Question {
        return when (gameType) {
            GameType.ADDITION -> generateAdditionQuestion(difficulty)
            GameType.SUBTRACTION -> generateSubtractionQuestion(difficulty)
            GameType.MULTIPLICATION -> generateMultiplicationQuestion(difficulty)
            GameType.DIVISION -> generateDivisionQuestion(difficulty)
            GameType.MIXED -> generateMixedQuestion(difficulty)
            GameType.BRAIN_TEASER -> generateBrainTeaser(difficulty)
        }
    }
    
    private fun generateMultiplicationQuestion(difficulty: Int): Question {
        val num1 = getRandomNumber(difficulty)
        val num2 = getRandomNumber(difficulty)
        val answer = num1 * num2
        
        return Question(
            text = "$num1 × $num2 = ?",
            correctAnswer = answer,
            options = generateOptions(answer), // Wrong answers nearby
            difficulty = difficulty,
            
            // Progressive hints
            hint = "Think of this as adding $num1, $num2 times",
            
            detailedExplanation = """
                Multiplication is repeated addition:
                $num1 × $num2 means add $num1 together $num2 times
                
                ${(1..num2).joinToString(" + ") { "$num1" }} = $answer
            """.trimIndent(),
            
            fullExplanation = """
                Multiplication: $num1 × $num2
                
                Method 1 - Repeated Addition:
                ${(1..num2).joinToString(" + ") { "$num1" }} = $answer
                
                Method 2 - Using multiplication tables:
                $num1 times $num2 = $answer
                
                Tip: Memorizing multiplication tables makes this faster!
            """.trimIndent()
        )
    }
    
    // Generate plausible wrong answers
    private fun generateOptions(correctAnswer: Int): List<Int> {
        val options = mutableListOf(correctAnswer)
        
        // Add wrong answers close to correct one
        options.add(correctAnswer + Random.nextInt(1, 10))
        options.add(correctAnswer - Random.nextInt(1, 10))
        options.add(correctAnswer + Random.nextInt(10, 20))
        
        return options.shuffled()
    }
    
    // Difficulty affects number ranges
    private fun getRandomNumber(difficulty: Int): Int {
        return when (difficulty) {
            1 -> Random.nextInt(1, 10)      // 1-9
            2 -> Random.nextInt(10, 50)     // 10-49
            3 -> Random.nextInt(50, 100)    // 50-99
            4 -> Random.nextInt(100, 500)   // 100-499
            else -> Random.nextInt(1, 1000) // 1-999
        }
    }
}
```

**Design decisions:**
- **Educational hints**: 3 levels (hint → detailed → full) for progressive learning
- **Plausible distractors**: Wrong answers close to correct one (not random)
- **Difficulty scaling**: Number ranges increase with difficulty
- **Pure functions**: No state, easy to test

#### **SudokuEngine.kt** - Sudoku puzzle generation

```kotlin
class SudokuEngine {
    
    // Generate a solvable Sudoku puzzle
    fun generatePuzzle(difficulty: Difficulty): SudokuPuzzle {
        // 1. Generate complete valid solution
        val solution = generateSolution()
        
        // 2. Remove numbers based on difficulty
        val puzzle = removeNumbers(solution.copy(), difficulty.cellsToRemove)
        
        // 3. Ensure puzzle has unique solution
        if (!hasUniqueSolution(puzzle)) {
            return generatePuzzle(difficulty) // Retry
        }
        
        return SudokuPuzzle(
            board = puzzle,
            solution = solution,
            difficulty = difficulty
        )
    }
    
    // Generate complete solved Sudoku
    private fun generateSolution(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        solveSudoku(board, 0, 0)
        return board
    }
    
    // Backtracking algorithm
    private fun solveSudoku(board: Array<IntArray>, row: Int, col: Int): Boolean {
        // Base case: reached end of board
        if (row == 9) return true
        
        // Move to next row
        if (col == 9) return solveSudoku(board, row + 1, 0)
        
        // Cell already filled
        if (board[row][col] != 0) return solveSudoku(board, row, col + 1)
        
        // Try numbers 1-9 in random order (for variety)
        val numbers = (1..9).shuffled()
        for (num in numbers) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                
                // Recursively solve
                if (solveSudoku(board, row, col + 1)) return true
                
                // Backtrack
                board[row][col] = 0
            }
        }
        
        return false // No valid solution found
    }
    
    // Validate number placement
    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        if (board[row].contains(num)) return false
        
        // Check column
        if ((0..8).any { board[it][col] == num }) return false
        
        // Check 3x3 box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        
        return true
    }
    
    // Find conflicting cells (for UI highlighting)
    fun findConflicts(board: Array<IntArray>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        
        for (row in 0..8) {
            for (col in 0..8) {
                val num = board[row][col]
                if (num != 0 && !isValidPlacement(board, row, col)) {
                    conflicts.add(Pair(row, col))
                }
            }
        }
        
        return conflicts
    }
}
```

**Algorithm explained:**
1. **Backtracking**: Classic Sudoku solving algorithm
2. **Shuffled numbers**: Random order creates variety
3. **Validation**: Row, column, 3×3 box checks
4. **Conflict detection**: For real-time UI feedback

---

### 3. ViewModel Layer (viewmodel/)

ViewModels hold UI state and handle business logic.

#### **GameViewModel.kt** - Main game state with smart hints

```kotlin
class GameViewModel(
    private val settingsManager: SettingsManager,
    private val achievementManager: AchievementManager,
    private val badgeManager: BadgeManager
) : ViewModel() {
    
    // UI State (exposed to UI)
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Initial)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    // Per-question attempt tracking for smart hints
    private val questionAttempts = mutableMapOf<Int, Int>()
    
    // Current hint state
    private val _currentHintState = MutableStateFlow<HintState>(HintState.None)
    val currentHintState: StateFlow<HintState> = _currentHintState.asStateFlow()
    
    // Start new game
    fun startGame(gameType: GameType, difficulty: Int, timeMode: TimeMode) {
        _uiState.value = GameUiState.Loading
        
        viewModelScope.launch {
            val questions = generateQuestions(gameType, difficulty, 10)
            
            _uiState.value = GameUiState.Playing(
                questions = questions,
                currentQuestionIndex = 0,
                score = 0,
                correctAnswers = 0,
                startTime = System.currentTimeMillis(),
                gameType = gameType,
                difficulty = difficulty,
                timeMode = timeMode
            )
        }
    }
    
    // Handle answer submission
    fun checkAnswer(answer: Int) {
        val state = _uiState.value as? GameUiState.Playing ?: return
        val currentQuestion = state.questions[state.currentQuestionIndex]
        val questionIndex = state.currentQuestionIndex
        
        // Get current attempts for this question
        val attempts = questionAttempts.getOrDefault(questionIndex, 0)
        
        if (answer == currentQuestion.correctAnswer) {
            // CORRECT ANSWER
            handleCorrectAnswer(state, currentQuestion)
            resetHintState()
        } else {
            // WRONG ANSWER - Progressive hints
            val newAttempts = attempts + 1
            questionAttempts[questionIndex] = newAttempts
            
            when (newAttempts) {
                1 -> {
                    // First wrong: just feedback
                    _currentHintState.value = HintState.FirstAttempt
                }
                2 -> {
                    // Second wrong: show hint
                    _currentHintState.value = HintState.ShowHint(
                        currentQuestion.hint
                    )
                }
                3 -> {
                    // Third wrong: show detailed explanation
                    _currentHintState.value = HintState.ShowDetailedHint(
                        currentQuestion.hint,
                        currentQuestion.detailedExplanation
                    )
                }
                else -> {
                    // Fourth+ wrong: show answer and auto-advance
                    _currentHintState.value = HintState.ShowAnswer(
                        currentQuestion.correctAnswer,
                        currentQuestion.fullExplanation
                    )
                    
                    // Auto-advance after 3 seconds
                    viewModelScope.launch {
                        delay(3000)
                        moveToNextQuestion()
                    }
                }
            }
        }
    }
    
    private fun handleCorrectAnswer(state: GameUiState.Playing, question: Question) {
        // Calculate score
        val timeElapsed = System.currentTimeMillis() - state.startTime
        val timeMultiplier = calculateTimeMultiplier(timeElapsed, state.timeMode)
        val baseScore = question.difficulty * 100
        val finalScore = (baseScore * timeMultiplier).toInt()
        
        // Update state
        val newState = state.copy(
            score = state.score + finalScore,
            correctAnswers = state.correctAnswers + 1
        )
        
        // Check if game complete
        if (newState.currentQuestionIndex == newState.questions.size - 1) {
            handleGameCompletion(newState)
        } else {
            moveToNextQuestion()
        }
    }
    
    private fun handleGameCompletion(state: GameUiState.Playing) {
        // Track achievements
        achievementManager.trackGameCompletion(
            score = state.score,
            totalQuestions = state.questions.size,
            correctAnswers = state.correctAnswers,
            timeMultiplier = calculateTimeMultiplier(
                System.currentTimeMillis() - state.startTime,
                state.timeMode
            )
        )
        
        // Update badges
        val gameStats = GameStats(
            quickGamesPlayed = if (state.timeMode == TimeMode.QUICK) 1 else 0,
            correctStreakCurrent = state.correctAnswers,
            perfectGamesCount = if (state.correctAnswers == state.questions.size) 1 else 0,
            dailyStreak = getUserDailyStreak(),
            challengeWins = 0
        )
        val newBadges = badgeManager.updateBadgeProgress(gameStats)
        
        // Show completion screen
        _uiState.value = GameUiState.Completed(
            finalScore = state.score,
            correctAnswers = state.correctAnswers,
            totalQuestions = state.questions.size,
            newBadges = newBadges
        )
    }
    
    private fun resetHintState() {
        _currentHintState.value = HintState.None
        questionAttempts.remove((_uiState.value as? GameUiState.Playing)?.currentQuestionIndex)
    }
}

// UI State sealed class
sealed class GameUiState {
    object Initial : GameUiState()
    object Loading : GameUiState()
    
    data class Playing(
        val questions: List<Question>,
        val currentQuestionIndex: Int,
        val score: Int,
        val correctAnswers: Int,
        val startTime: Long,
        val gameType: GameType,
        val difficulty: Int,
        val timeMode: TimeMode
    ) : GameUiState()
    
    data class Completed(
        val finalScore: Int,
        val correctAnswers: Int,
        val totalQuestions: Int,
        val newBadges: List<Badge>
    ) : GameUiState()
}

// Hint State sealed class
sealed class HintState {
    object None : HintState()
    object FirstAttempt : HintState()
    data class ShowHint(val hint: String) : HintState()
    data class ShowDetailedHint(val hint: String, val explanation: String) : HintState()
    data class ShowAnswer(val answer: Int, val fullExplanation: String) : HintState()
}
```

**Key ViewModel patterns:**
- **Sealed classes for state**: Type-safe state handling
- **StateFlow**: Reactive state updates to UI
- **viewModelScope**: Coroutines tied to ViewModel lifecycle
- **Per-question tracking**: Each question independently tracked for hints
- **Progressive hints**: Educational approach (hint → detailed → answer)
- **Auto-progression**: After 4 attempts, show answer and move on
- **Achievement/Badge integration**: Game completion triggers unlocks

#### **HomeViewModel.kt** - Home screen with badge display

```kotlin
class HomeViewModel(
    private val achievementManager: AchievementManager,
    private val badgeManager: BadgeManager,
    private val userPreferences: UserPreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        loadBadges()
        checkDailyBonus()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            val playerName = userPreferences.getPlayerName()
            val totalPoints = achievementManager.getTotalPoints()
            val rank = achievementManager.getCurrentRank()
            
            _uiState.value = _uiState.value.copy(
                playerName = playerName,
                totalPoints = totalPoints,
                rank = rank
            )
        }
    }
    
    private fun loadBadges() {
        viewModelScope.launch {
            val badges = badgeManager.getUnlockedBadges()
            
            _uiState.value = _uiState.value.copy(
                unlockedBadges = badges
            )
        }
    }
    
    private fun checkDailyBonus() {
        viewModelScope.launch {
            val lastLoginDate = userPreferences.getLastLoginDate()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())
            
            if (lastLoginDate != today) {
                userPreferences.setLastLoginDate(today)
                
                // Increment streak or reset
                val streak = if (isConsecutiveDay(lastLoginDate, today)) {
                    userPreferences.incrementDailyStreak()
                } else {
                    userPreferences.resetDailyStreak()
                    1
                }
                
                // Show daily bonus dialog
                _uiState.value = _uiState.value.copy(
                    showDailyBonus = true,
                    dailyStreak = streak
                )
            }
        }
    }
}

data class HomeUiState(
    val playerName: String = "",
    val totalPoints: Int = 0,
    val rank: Rank = Rank.Beginner,
    val unlockedBadges: List<Badge> = emptyList(),
    val showDailyBonus: Boolean = false,
    val dailyStreak: Int = 0
)
```

**Why this structure?**
- **Single data class for state**: All UI data in one place
- **Init block**: Load data on ViewModel creation
- **Daily bonus logic**: Streak tracking encourages daily engagement
- **Badge loading**: Provides data for BadgeRow component

---

### 4. UI Layer (ui/)

Jetpack Compose UI with reusable components.

#### **BadgeDisplay.kt** - Reusable badge components

```kotlin
// Badge Row - Horizontal display with overflow indicator
@Composable
fun BadgeRow(
    badges: List<Badge>,
    maxBadges: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show first N badges
        badges.take(maxBadges).forEach { badge ->
            BadgeIndicator(
                badge = badge,
                isUnlocked = true
            )
        }
        
        // Show "+X" if more badges exist
        if (badges.size > maxBadges) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${badges.size - maxBadges}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Badge Indicator - Single badge with rarity border
@Composable
fun BadgeIndicator(
    badge: Badge,
    isUnlocked: Boolean,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                if (isUnlocked) badge.rarity.color.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 2.dp,
                color = if (isUnlocked) badge.rarity.color else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badge.icon,
            fontSize = 32.sp,
            modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
        )
    }
}

// Badge Grid - Category-filtered badge collection
@Composable
fun BadgeGrid(
    badges: List<Badge>,
    unlockedBadgeIds: Set<String>,
    onBadgeClick: (Badge) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(badges) { badge ->
            val isUnlocked = badge.id in unlockedBadgeIds
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgeIndicator(
                    badge = badge,
                    isUnlocked = isUnlocked,
                    onClick = { onBadgeClick(badge) }
                )
                
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

**Component design principles:**
- **Composability**: Small components compose into larger ones
- **Parameterization**: Components accept data, not ViewModels
- **Stateless**: No internal state, everything from parameters
- **Reusable**: Same components used across multiple screens

#### **GameScreen.kt** - Main gameplay screen with hints

```kotlin
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hintState by viewModel.currentHintState.collectAsState()
    
    when (uiState) {
        is GameUiState.Loading -> {
            LoadingScreen()
        }
        
        is GameUiState.Playing -> {
            val state = uiState as GameUiState.Playing
            val currentQuestion = state.questions[state.currentQuestionIndex]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                GameHeader(
                    score = state.score,
                    questionNumber = state.currentQuestionIndex + 1,
                    totalQuestions = state.questions.size
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Question
                QuestionCard(
                    question = currentQuestion,
                    onAnswerSelected = { answer ->
                        viewModel.checkAnswer(answer)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hint display (progressive)
                HintDisplay(hintState = hintState)
            }
        }
        
        is GameUiState.Completed -> {
            val state = uiState as GameUiState.Completed
            
            CompletionScreen(
                score = state.finalScore,
                correctAnswers = state.correctAnswers,
                totalQuestions = state.totalQuestions,
                newBadges = state.newBadges,
                onPlayAgain = { viewModel.startNewGame() },
                onBackToHome = onNavigateBack
            )
        }
    }
}

// Hint display with color-coded cards
@Composable
fun HintDisplay(hintState: HintState) {
    when (hintState) {
        is HintState.None -> { /* No hint */ }
        
        is HintState.FirstAttempt -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "❌ Not quite! Try again.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        is HintState.ShowHint -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Blue.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hint:",
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue
                        )
                    }
                    Text(
                        text = hintState.hint,
                        color = Color.Blue
                    )
                }
            }
        }
        
        is HintState.ShowDetailedHint -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Orange.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detailed Hint:",
                            fontWeight = FontWeight.Bold,
                            color = Color.Orange
                        )
                    }
                    Text(
                        text = hintState.hint,
                        color = Color.Orange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hintState.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Orange
                    )
                }
            }
        }
        
        is HintState.ShowAnswer -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✓", fontSize = 32.sp, color = Color.Green)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Answer: ${hintState.answer}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Green
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hintState.fullExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Moving to next question...",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = Color.Green
                    )
                }
            }
        }
    }
}
```

**UI/UX patterns:**
- **State-driven UI**: UI renders based on StateFlow state
- **Sealed class matching**: Type-safe when expressions
- **Color coding**: Visual progression (Red → Blue → Orange → Green)
- **Feedback hierarchy**: Increasing levels of help
- **Auto-progression message**: User knows answer will auto-advance

---

## State Management Patterns

### StateFlow vs LiveData

The app uses **StateFlow** instead of LiveData:

**Advantages:**
- **Kotlin-first**: Better Kotlin integration
- **Operators**: Map, filter, combine, etc.
- **Collect in Composables**: `.collectAsState()` for Compose
- **Initial value**: Always has a value (no null checks)

**Example:**

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// UI
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // uiState automatically updates on recomposition
    Text(text = uiState.playerName)
}
```

### Immutable State Updates

Always use `.copy()` to update data classes:

```kotlin
// ❌ BAD - Mutates state
_uiState.value.score += 100

// ✅ GOOD - Creates new instance
_uiState.value = _uiState.value.copy(
    score = _uiState.value.score + 100
)
```

**Why?**
- Immutability prevents bugs
- StateFlow only emits if reference changes
- Easier to debug (state snapshots)

---

## Firebase Integration

### Firestore Structure

```
users/
  {userId}/
    name: "PlayerName"
    totalPoints: 12500
    rank: "Expert"
    createdAt: Timestamp
    
    highScores/
      {scoreId}/
        score: 8500
        gameType: "MULTIPLICATION"
        difficulty: 3
        timestamp: Timestamp
    
    badges/
      {badgeId}/
        unlockedAt: Timestamp

groups/
  {groupId}/
    name: "Math Masters"
    code: "ABC123"
    createdBy: "userId"
    members: ["userId1", "userId2"]
    
    leaderboard/
      {userId}/
        score: 5000
        lastUpdated: Timestamp

challenges/
  {challengeId}/
    challenger: "userId1"
    opponent: "userId2"
    status: "pending"
    questions: [...]
    results: {...}
```

### Firebase Service Example

```kotlin
class FirebaseScoreService {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    
    suspend fun uploadScore(score: HighScore): String = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid ?: run {
            continuation.resumeWithException(Exception("Not authenticated"))
            return@suspendCoroutine
        }
        
        firestore.collection("users").document(userId)
            .collection("highScores")
            .add(score)
            .addOnSuccessListener { documentReference ->
                continuation.resume(documentReference.id)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    suspend fun fetchScores(): List<HighScore> = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid ?: run {
            continuation.resumeWithException(Exception("Not authenticated"))
            return@suspendCoroutine
        }
        
        firestore.collection("users").document(userId)
            .collection("highScores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val scores = documents.map { it.toObject(HighScore::class.java) }
                continuation.resume(scores)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}
```

---

## Navigation System

### NavGraph Setup

```kotlin
// MainActivity.kt
@Composable
fun MathWorkoutApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToGame = { gameType ->
                    navController.navigate("game/$gameType")
                }
            )
        }
        
        composable(
            route = "game/{gameType}",
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameType = backStackEntry.arguments?.getString("gameType")
            GameScreen(
                viewModel = gameViewModel,
                gameType = gameType,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("badges") {
            BadgesScreen(
                viewModel = badgeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // ... more routes
    }
}
```

---

## Best Practices & Patterns

### 1. **Separation of Concerns**
- UI only renders, no business logic
- ViewModels handle logic, no UI code
- Repositories abstract data sources

### 2. **Dependency Injection**
```kotlin
class GameViewModel(
    private val settingsManager: SettingsManager,  // Injected
    private val achievementManager: AchievementManager  // Injected
) : ViewModel()
```

### 3. **Error Handling**
```kotlin
try {
    val result = repository.fetchData()
    _uiState.value = UiState.Success(result)
} catch (e: Exception) {
    _uiState.value = UiState.Error(e.message ?: "Unknown error")
}
```

### 4. **Coroutine Scopes**
- `viewModelScope`: For ViewModel operations
- `CoroutineScope(Dispatchers.IO)`: For background tasks
- `Dispatchers.Main`: For UI updates

### 5. **Compose Remember**
```kotlin
// ❌ BAD - Recreates on every recomposition
val badges = badgeManager.getUnlockedBadges()

// ✅ GOOD - Remembers across recompositions
val badges = remember { badgeManager.getUnlockedBadges() }
```

### 6. **Sealed Classes for State**
```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### 7. **Extension Functions**
```kotlin
fun Long.toFormattedTime(): String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    return String.format("%02d:%02d", minutes, seconds)
}
```

---

## Conclusion

This codebase follows modern Android best practices with:
- **Clean Architecture**: Clear layer separation
- **Reactive State**: StateFlow for responsive UI
- **Testable Code**: ViewModels and repositories easily testable
- **Educational Focus**: Smart hints system, educational content
- **Scalable Design**: Easy to add new game modes, badges, themes
- **Offline-First**: Local data with background sync
- **User Engagement**: Gamification, achievements, social features

The architecture supports future growth while maintaining code quality and user experience.
