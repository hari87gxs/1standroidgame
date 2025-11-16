# Athreya's Math Workout - Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Layer Structure](#layer-structure)
4. [Data Flow](#data-flow)
5. [Component Details](#component-details)
6. [State Management](#state-management)
7. [Navigation](#navigation)
8. [Database Schema](#database-schema)
9. [Firebase Integration](#firebase-integration)
10. [Theme System](#theme-system)
11. [Achievement System](#achievement-system)
12. [Security & Privacy](#security--privacy)
13. [Performance Optimizations](#performance-optimizations)
14. [Testing Strategy](#testing-strategy)

---

## Overview

Athreya's Math Workout is a modern Android application built using **MVVM (Model-View-ViewModel)** architecture with **Jetpack Compose** for UI. The app follows clean architecture principles, separating concerns into distinct layers for maintainability, testability, and scalability.

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | Jetpack Compose | Declarative UI framework |
| **Architecture** | MVVM | Separation of concerns |
| **Navigation** | Compose Navigation | Screen routing |
| **State Management** | StateFlow, MutableState | Reactive state handling |
| **Local Database** | Room | Persistent local storage |
| **Remote Database** | Firebase Firestore | Cloud data sync |
| **Authentication** | Firebase Auth | User identity |
| **Settings** | SharedPreferences, DataStore | User preferences |
| **Dependency Injection** | Manual (ViewModelFactory) | Dependency management |
| **Asynchronous** | Kotlin Coroutines | Background operations |
| **Language** | Kotlin 1.9.0 | Main programming language |

### Design Principles

1. **Separation of Concerns**: Each layer has a single, well-defined responsibility
2. **Unidirectional Data Flow**: Data flows from ViewModel → UI, events flow UI → ViewModel
3. **Single Source of Truth**: ViewModels hold the UI state
4. **Immutability**: Use of data classes and StateFlow for predictable state
5. **Reactive Programming**: StateFlow and Compose state for reactive UI updates
6. **Dependency Inversion**: High-level modules don't depend on low-level modules

---

## Architecture Patterns

### MVVM (Model-View-ViewModel)

```
┌──────────────────────────────────────────────────────────────┐
│                         View Layer                            │
│              (Composable Functions - UI)                      │
│                                                               │
│  Observes State ↓          ↑ Emits Events                    │
├──────────────────────────────────────────────────────────────┤
│                      ViewModel Layer                          │
│         (Business Logic & State Management)                   │
│                                                               │
│  Reads/Writes ↓            ↑ Returns Data                     │
├──────────────────────────────────────────────────────────────┤
│                      Repository Layer                         │
│            (Data Source Abstraction)                          │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Local DB    │  │   Firebase   │  │ Preferences  │       │
│  │   (Room)     │  │  (Firestore) │  │  (DataStore) │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└──────────────────────────────────────────────────────────────┘
```

### Repository Pattern

Each data source (Room, Firebase, Preferences) is accessed through a Repository interface, providing:
- **Abstraction**: ViewModels don't know about data source implementation
- **Testability**: Easy to mock repositories for testing
- **Flexibility**: Can swap data sources without changing ViewModels
- **Caching**: Repositories can implement caching strategies

---

## Layer Structure

### 1. UI Layer (View)

**Location**: `ui/screens/` and `ui/components/`

**Responsibilities**:
- Render UI based on ViewModel state
- Collect user input and emit events to ViewModel
- Handle UI-specific logic (animations, transitions)
- Compose reusable components

**Key Components**:
```kotlin
// Screen Composables
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // UI rendering based on state
    when (uiState) {
        is HomeUiState.Loading -> LoadingScreen()
        is HomeUiState.Success -> Content(uiState.data)
        is HomeUiState.Error -> ErrorScreen(uiState.message)
    }
}
```

**Design Patterns**:
- **Composition over Inheritance**: Compose functions are composable
- **Stateless Components**: UI components are stateless, state comes from ViewModel
- **Side Effects**: Use `LaunchedEffect`, `DisposableEffect` for side effects
- **Remember**: Use `remember`, `rememberSaveable` for UI state that survives recomposition

### 2. ViewModel Layer

**Location**: `viewmodel/`

**Responsibilities**:
- Hold and manage UI state
- Handle business logic
- Expose state to UI via StateFlow
- Coordinate Repository calls
- Handle user actions/events

**Example**:
```kotlin
class GameViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Initial)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    // User Actions
    fun onAnswerSubmitted(answer: Int) {
        viewModelScope.launch {
            val isCorrect = checkAnswer(answer)
            updateGameState(isCorrect)
        }
    }
    
    // Business Logic
    private fun checkAnswer(answer: Int): Boolean {
        // Validation logic
    }
}
```

**State Management**:
- `MutableStateFlow` for internal state mutations
- `StateFlow.asStateFlow()` for read-only external exposure
- `viewModelScope` for coroutines that live with ViewModel
- Sealed classes for UI state types

### 3. Repository Layer

**Location**: `data/repository/`

**Responsibilities**:
- Abstract data source details
- Coordinate between multiple data sources
- Implement caching strategies
- Handle data transformation
- Error handling and retry logic

**Example**:
```kotlin
interface ScoreRepository {
    suspend fun getHighScores(): List<HighScore>
    suspend fun saveScore(score: HighScore)
    suspend fun syncWithFirebase()
}

class ScoreRepositoryImpl(
    private val localDao: HighScoreDao,
    private val firebaseService: FirebaseScoreService
) : ScoreRepository {
    
    override suspend fun getHighScores(): List<HighScore> {
        // Try local first, fallback to Firebase
        return localDao.getAll().ifEmpty {
            firebaseService.fetchScores().also {
                localDao.insertAll(it)
            }
        }
    }
}
```

### 4. Data Layer

**Location**: `data/`

**Components**:
- **Entities**: Room database entities (`HighScore`, `Group`, `Challenge`)
- **DAOs**: Database Access Objects for queries
- **Models**: Data transfer objects
- **Services**: Firebase, API services
- **Managers**: Specialized data handlers (`AchievementManager`, `ThemePreferencesManager`)

---

## Data Flow

### User Interaction Flow

```
User Action (Click Button)
    ↓
Composable Function (Emits Event)
    ↓
ViewModel (Handles Event)
    ↓
Repository (Fetches/Updates Data)
    ↓
Data Source (Room/Firebase/Preferences)
    ↓
Repository (Returns Result)
    ↓
ViewModel (Updates State)
    ↓
StateFlow (Emits New State)
    ↓
Composable (Recomposes with New State)
    ↓
UI Update (User Sees Result)
```

### Example: Submitting an Answer

1. **User taps answer button**
2. **GameScreen** calls `viewModel.onAnswerSubmitted(answer)`
3. **GameViewModel** validates answer and updates score
4. **GameViewModel** calls `achievementManager.trackGameCompletion()`
5. **AchievementManager** checks for new achievements
6. **AchievementManager** updates StateFlow if achievements unlocked
7. **GameViewModel** updates `_uiState` with new score
8. **GameScreen** observes state change and recomposes
9. **User sees** updated score and possibly achievement notification

---

## Component Details

### MainActivity

**Purpose**: App entry point, navigation setup, theme management

**Key Responsibilities**:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            // Theme selection
            val currentThemeId by themeManager
                .currentTheme
                .collectAsState(initial = "default")
            
            // Apply theme
            AthreyasSumsTheme(themeId = currentThemeId) {
                MathWorkoutApp(
                    onThemeChanged = { themeManager.setTheme(it) }
                )
            }
        }
    }
}
```

**Features**:
- Edge-to-edge display
- Dynamic theme switching
- ViewModel creation via factories
- Navigation setup

### Navigation System

**File**: `navigation/Screen.kt`

**Architecture**:
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game/{mode}/{difficulty}") {
        fun createRoute(mode: String, difficulty: String) = 
            "game/$mode/$difficulty"
    }
    object Results : Screen("results/{score}/{total}")
    object Achievements : Screen("achievements")
    object ThemeSelector : Screen("themes")
    object DailyChallenge : Screen("daily_challenge")
    object GlobalLeaderboard : Screen("global_leaderboard")
    object Groups : Screen("groups")
}
```

**Navigation Graph**:
```kotlin
@Composable
fun MathWorkoutNavigation(
    navController: NavHostController,
    // ... ViewModels
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { 
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")
            val difficulty = backStackEntry.arguments?.getString("difficulty")
            
            GameScreen(
                mode = mode,
                difficulty = difficulty,
                onComplete = { score, total ->
                    navController.navigate(
                        Screen.Results.createRoute(score, total)
                    )
                }
            )
        }
    }
}
```

---

## State Management

### ViewModel State Pattern

**UI State Classes**:
```kotlin
sealed class GameUiState {
    object Initial : GameUiState()
    object Loading : GameUiState()
    
    data class Playing(
        val currentQuestion: Question,
        val questionNumber: Int,
        val totalQuestions: Int,
        val score: Int,
        val timeElapsed: Long,
        val multiplier: Float
    ) : GameUiState()
    
    data class Finished(
        val finalScore: Int,
        val totalQuestions: Int,
        val correctAnswers: Int,
        val timeElapsed: Long,
        val isNewRecord: Boolean
    ) : GameUiState()
    
    data class Error(val message: String) : GameUiState()
}
```

**ViewModel Implementation**:
```kotlin
class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Initial)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    fun startGame(difficulty: Difficulty, questionCount: Int) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading
            
            try {
                val questions = generateQuestions(difficulty, questionCount)
                _uiState.value = GameUiState.Playing(
                    currentQuestion = questions.first(),
                    questionNumber = 1,
                    totalQuestions = questionCount,
                    score = 0,
                    timeElapsed = 0,
                    multiplier = 1.0f
                )
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### State Collection in UI

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is GameUiState.Loading -> LoadingIndicator()
        
        is GameUiState.Playing -> {
            QuestionCard(
                question = state.currentQuestion,
                questionNumber = state.questionNumber,
                totalQuestions = state.totalQuestions,
                score = state.score,
                onAnswer = { answer -> 
                    viewModel.onAnswerSubmitted(answer) 
                }
            )
        }
        
        is GameUiState.Finished -> {
            ResultsCard(
                score = state.finalScore,
                total = state.totalQuestions,
                isNewRecord = state.isNewRecord
            )
        }
        
        is GameUiState.Error -> ErrorMessage(state.message)
    }
}
```

---

## Database Schema

### Room Database Structure

**Database File**: `data/AppDatabase.kt`

```kotlin
@Database(
    entities = [
        HighScore::class,
        Group::class,
        GroupMember::class,
        Challenge::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun challengeDao(): ChallengeDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "athreya_math_workout.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
```

### Entities

#### HighScore Entity
```kotlin
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val difficulty: String,
    val mode: String,
    val timeElapsed: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val playerName: String? = null,
    val isNewRecord: Boolean = false
)
```

#### Group Entity
```kotlin
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val description: String = "",
    val creatorId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 1,
    val imageUrl: String? = null,
    val isPublic: Boolean = true
)
```

#### Challenge Entity
```kotlin
@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val title: String,
    val description: String,
    val creatorId: String,
    val targetScore: Int,
    val difficulty: String,
    val questionCount: Int,
    val timeLimit: Long? = null,
    val expiresAt: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "active" // active, completed, expired
)
```

### DAOs (Data Access Objects)

```kotlin
@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT :limit")
    suspend fun getTopScores(limit: Int = 10): List<HighScore>
    
    @Query("SELECT * FROM high_scores WHERE difficulty = :difficulty ORDER BY score DESC LIMIT 1")
    suspend fun getHighestScore(difficulty: String): HighScore?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highScore: HighScore): Long
    
    @Query("DELETE FROM high_scores")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM high_scores")
    suspend fun getCount(): Int
}
```

---

## Firebase Integration

### Firestore Structure

```
users/
  {userId}/
    username: string
    displayName: string
    createdAt: timestamp
    totalGames: number
    totalPoints: number
    currentRank: string
    
global_scores/
  {scoreId}/
    userId: string
    playerName: string
    score: number
    difficulty: string
    mode: string
    timestamp: timestamp
    country: string
    
groups/
  {groupId}/
    name: string
    description: string
    creatorId: string
    memberCount: number
    createdAt: timestamp
    
    members/
      {userId}/
        joinedAt: timestamp
        role: string
        
challenges/
  {challengeId}/
    title: string
    description: string
    creatorId: string
    targetScore: number
    difficulty: string
    expiresAt: timestamp
    participants: array
    
daily_challenges/
  {date}/
    problems: array
    difficulty: string
    expiresAt: timestamp
    completions: number
```

### Firebase Services

**FirebaseScoreService**:
```kotlin
class FirebaseScoreService {
    private val firestore = FirebaseFirestore.getInstance()
    private val scoresCollection = firestore.collection("global_scores")
    
    suspend fun submitScore(score: FirebaseHighScore): Result<Unit> {
        return try {
            scoresCollection
                .document()
                .set(score)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTopScores(limit: Int = 100): Result<List<FirebaseHighScore>> {
        return try {
            val snapshot = scoresCollection
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                
            val scores = snapshot.documents.mapNotNull {
                it.toObject(FirebaseHighScore::class.java)
            }
            
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // User data - users can only read/write their own data
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Global scores - anyone can read, authenticated users can write
    match /global_scores/{scoreId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null 
        && resource.data.userId == request.auth.uid;
    }
    
    // Groups - public groups readable by all
    match /groups/{groupId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null 
        && resource.data.creatorId == request.auth.uid;
        
      // Group members
      match /members/{userId} {
        allow read: if true;
        allow write: if request.auth != null;
      }
    }
    
    // Challenges - readable by all, writable by creator
    match /challenges/{challengeId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null 
        && resource.data.creatorId == request.auth.uid;
    }
    
    // Daily challenges - read by all, write by admin only
    match /daily_challenges/{date} {
      allow read: if true;
      allow write: if false; // Admin only via console
    }
  }
}
```

### Firestore Indexes

```json
{
  "indexes": [
    {
      "collectionGroup": "global_scores",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "score", "order": "DESCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "global_scores",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "difficulty", "order": "ASCENDING" },
        { "fieldPath": "score", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "groups",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "isPublic", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## Theme System

### Theme Architecture

**Theme Definitions**: `ui/theme/AppTheme.kt`

```kotlin
object Themes {
    val Default = AppTheme(
        id = "default",
        name = "Default",
        lightColorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFFFFBFE),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color.White,
            onSecondary = Color.Black
        ),
        darkColorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF121212)
        ),
        isUnlocked = true
    )
    
    val Marvel = AppTheme(
        id = "marvel",
        name = "Marvel",
        lightColorScheme = lightColorScheme(
            primary = Color(0xFFE23636),
            secondary = Color(0xFF0476F2),
            // ...
        ),
        unlockRequirement = "Score 10,000+ in a single game",
        isUnlocked = false
    )
    
    // ... more themes
    
    fun getAllThemes() = listOf(
        Default, Dark, Marvel, DC, Neon, Ocean, Sunset
    )
    
    fun getThemeById(id: String) = 
        getAllThemes().find { it.id == id } ?: Default
}
```

### Theme Manager

**File**: `data/ThemePreferencesManager.kt`

```kotlin
class ThemePreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences(
        "theme_prefs",
        Context.MODE_PRIVATE
    )
    
    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()
    
    fun setTheme(themeId: String) {
        prefs.edit().putString(CURRENT_THEME_KEY, themeId).apply()
        _currentTheme.value = themeId
    }
    
    fun unlockTheme(themeId: String) {
        val unlockedThemes = getUnlockedThemes().toMutableSet()
        unlockedThemes.add(themeId)
        prefs.edit()
            .putStringSet(UNLOCKED_THEMES_KEY, unlockedThemes)
            .apply()
    }
    
    fun isThemeUnlocked(themeId: String): Boolean {
        return getUnlockedThemes().contains(themeId) || 
               Themes.getThemeById(themeId).isUnlocked
    }
    
    private fun getUnlockedThemes(): Set<String> {
        return prefs.getStringSet(UNLOCKED_THEMES_KEY, emptySet()) ?: emptySet()
    }
}
```

### Theme Application

**File**: `ui/theme/Theme.kt`

```kotlin
@Composable
fun AthreyasSumsTheme(
    themeId: String = "default",
    content: @Composable () -> Unit
) {
    val theme = Themes.getThemeById(themeId)
    val isDarkMode = isSystemInDarkTheme()
    
    val colorScheme = if (isDarkMode) {
        theme.darkColorScheme
    } else {
        theme.lightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Achievement System

### Achievement Data Model

**File**: `data/Achievement.kt`

```kotlin
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val xpReward: Int,
    val requirement: Int,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedThemeId: String? = null
) {
    val progressPercentage: Float
        get() = (currentProgress.toFloat() / requirement).coerceIn(0f, 1f)
    
    companion object {
        fun getAllAchievements() = listOf(
            Achievement(
                id = "quick_learner",
                title = "Quick Learner",
                description = "Complete your first game",
                icon = "🎯",
                xpReward = 100,
                requirement = 1
            ),
            Achievement(
                id = "speed_demon",
                title = "Speed Demon",
                description = "Complete 30 games with 3× time multiplier",
                icon = "⚡",
                xpReward = 500,
                requirement = 30,
                unlockedThemeId = "neon"
            ),
            // ... more achievements
        )
    }
}
```

### Achievement Manager

**File**: `data/AchievementManager.kt`

```kotlin
class AchievementManager(private val context: Context) {
    private val prefs = context.getSharedPreferences(
        "achievement_prefs",
        Context.MODE_PRIVATE
    )
    
    private val _unlockedAchievements = MutableStateFlow<Set<String>>(
        loadUnlockedAchievements()
    )
    val unlockedAchievements: StateFlow<Set<String>> = 
        _unlockedAchievements.asStateFlow()
    
    private val _newlyUnlockedAchievements = MutableStateFlow<List<Achievement>>(
        emptyList()
    )
    val newlyUnlockedAchievements: StateFlow<List<Achievement>> = 
        _newlyUnlockedAchievements.asStateFlow()
    
    // Statistics tracking
    var totalGamesPlayed: Int
        get() = prefs.getInt("total_games", 0)
        private set(value) = prefs.edit().putInt("total_games", value).apply()
    
    var perfectGamesCount: Int
        get() = prefs.getInt("perfect_games", 0)
        private set(value) = prefs.edit().putInt("perfect_games", value).apply()
    
    var speedMultiplier3xCount: Int
        get() = prefs.getInt("speed_3x_count", 0)
        private set(value) = prefs.edit().putInt("speed_3x_count", value).apply()
    
    // Track game completion and check for achievements
    fun trackGameCompletion(
        score: Int,
        totalQuestions: Int,
        correctAnswers: Int,
        timeMultiplier: Float
    ) {
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
        
        checkAndUnlockAchievements()
    }
    
    private fun checkAndUnlockAchievements() {
        val newlyUnlocked = mutableListOf<Achievement>()
        val achievements = Achievement.getAllAchievements()
        
        achievements.forEach { achievement ->
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
                    
                    // Unlock theme if achievement has one
                    achievement.unlockedThemeId?.let { themeId ->
                        ThemePreferencesManager(context).unlockTheme(themeId)
                    }
                }
            }
        }
        
        if (newlyUnlocked.isNotEmpty()) {
            _newlyUnlockedAchievements.value = newlyUnlocked
        }
    }
    
    fun getCurrentRank(): Rank {
        return Rank.getRankForPoints(getTotalPoints())
    }
    
    fun getTotalPoints(): Int {
        return totalPointsAccumulated
    }
}
```

### Rank System

**File**: `data/Rank.kt`

```kotlin
data class Rank(
    val id: Int,
    val name: String,
    val icon: String,
    val minPoints: Int,
    val maxPoints: Int?,
    val color: Color,
    val description: String
) {
    companion object {
        val Beginner = Rank(
            id = 0,
            name = "Beginner",
            icon = "🌱",
            minPoints = 0,
            maxPoints = 999,
            color = Color(0xFF4CAF50),
            description = "Just starting your math journey"
        )
        
        val Amateur = Rank(
            id = 1,
            name = "Amateur",
            icon = "📚",
            minPoints = 1000,
            maxPoints = 4999,
            color = Color(0xFF2196F3),
            description = "Building your skills"
        )
        
        val Expert = Rank(
            id = 2,
            name = "Expert",
            icon = "🎓",
            minPoints = 5000,
            maxPoints = 14999,
            color = Color(0xFF9C27B0),
            description = "Mastering mental math"
        )
        
        val Master = Rank(
            id = 3,
            name = "Master",
            icon = "⚡",
            minPoints = 15000,
            maxPoints = 49999,
            color = Color(0xFFFF9800),
            description = "Elite calculation skills"
        )
        
        val Grandmaster = Rank(
            id = 4,
            name = "Grandmaster",
            icon = "👑",
            minPoints = 50000,
            maxPoints = null,
            color = Color(0xFFFFD700),
            description = "Math legend status"
        )
        
        fun getAllRanks() = listOf(
            Beginner, Amateur, Expert, Master, Grandmaster
        )
        
        fun getRankForPoints(points: Int): Rank {
            return getAllRanks().lastOrNull { points >= it.minPoints } 
                ?: Beginner
        }
        
        fun getNextRank(currentRank: Rank): Rank? {
            val allRanks = getAllRanks()
            val currentIndex = allRanks.indexOf(currentRank)
            return allRanks.getOrNull(currentIndex + 1)
        }
        
        fun getProgressToNextRank(currentPoints: Int): Float {
            val currentRank = getRankForPoints(currentPoints)
            val nextRank = getNextRank(currentRank) ?: return 1f
            
            val rangeSize = nextRank.minPoints - currentRank.minPoints
            val progressInRange = currentPoints - currentRank.minPoints
            
            return progressInRange.toFloat() / rangeSize
        }
    }
}
```

---

## Security & Privacy

### Data Protection

1. **Local Data**:
   - Room database encrypted at rest (device encryption)
   - SharedPreferences for non-sensitive data only
   - No passwords or credentials stored locally

2. **Firebase Authentication**:
   - Anonymous authentication for quick start
   - User ID never exposed in UI
   - Session management handled by Firebase SDK

3. **API Communication**:
   - HTTPS only for all network requests
   - Firebase security rules enforce data access
   - Input validation on all user-submitted data

### Privacy Policy

- No personal information collected beyond what's necessary
- Anonymous analytics only (Firebase Analytics)
- User can delete all data via settings
- No third-party data sharing
- GDPR compliant

---

## Performance Optimizations

### UI Performance

1. **Compose Optimizations**:
   ```kotlin
   // Stable data classes
   @Stable
   data class GameState(...)
   
   // Remember expensive computations
   val expensiveValue = remember(key) {
       computeExpensiveValue()
   }
   
   // Derived state
   val derivedState = remember(dependency) {
       derivedStateOf { transformation(dependency) }
   }
   ```

2. **LazyColumn/Grid**:
   - Use `key` parameter for stable identity
   - Implement `contentType` for heterogeneous lists
   - Use `derivedStateOf` for derived list data

3. **Image Loading**:
   - Use `rememberAsyncImagePainter` for network images
   - Implement proper placeholder and error states
   - Cache images appropriately

### Database Performance

1. **Indexes**:
   ```kotlin
   @Entity(
       tableName = "high_scores",
       indices = [
           Index(value = ["difficulty"]),
           Index(value = ["score"])
       ]
   )
   ```

2. **Batch Operations**:
   ```kotlin
   @Transaction
   suspend fun insertMultipleScores(scores: List<HighScore>) {
       scores.forEach { insert(it) }
   }
   ```

3. **Query Optimization**:
   - Use LIMIT for large result sets
   - Avoid SELECT * when possible
   - Use compiled queries for frequent operations

### Network Performance

1. **Caching Strategy**:
   - Cache Firebase data locally
   - Implement offline-first approach
   - Use stale-while-revalidate pattern

2. **Batch Writes**:
   - Batch multiple Firestore operations
   - Use transactions for related writes
   - Implement retry logic with exponential backoff

---

## Testing Strategy

### Unit Tests (To Be Implemented)

**ViewModel Tests**:
```kotlin
class GameViewModelTest {
    private lateinit var viewModel: GameViewModel
    private lateinit var settingsManager: FakeSettingsManager
    
    @Before
    fun setup() {
        settingsManager = FakeSettingsManager()
        viewModel = GameViewModel(settingsManager)
    }
    
    @Test
    fun `startGame should emit Playing state`() = runTest {
        viewModel.startGame(Difficulty.Easy, 10)
        
        val state = viewModel.uiState.value
        assertTrue(state is GameUiState.Playing)
        assertEquals(10, (state as GameUiState.Playing).totalQuestions)
    }
    
    @Test
    fun `correct answer should increase score`() = runTest {
        viewModel.startGame(Difficulty.Easy, 10)
        val initialState = viewModel.uiState.value as GameUiState.Playing
        
        viewModel.onAnswerSubmitted(initialState.currentQuestion.answer)
        
        val newState = viewModel.uiState.value as GameUiState.Playing
        assertTrue(newState.score > initialState.score)
    }
}
```

**Repository Tests**:
```kotlin
class ScoreRepositoryTest {
    private lateinit var repository: ScoreRepositoryImpl
    private lateinit var dao: FakeHighScoreDao
    private lateinit var firebaseService: FakeFirebaseService
    
    @Test
    fun `getHighScores should return local data first`() = runTest {
        val localScores = listOf(HighScore(score = 100))
        dao.setScores(localScores)
        
        val result = repository.getHighScores()
        
        assertEquals(localScores, result)
        assertFalse(firebaseService.wasCalled)
    }
}
```

### Integration Tests

**Database Tests**:
```kotlin
@RunWith(AndroidJUnit4::class)
class HighScoreDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: HighScoreDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.highScoreDao()
    }
    
    @Test
    fun insertAndRetrieveScore() = runBlocking {
        val score = HighScore(score = 100, totalQuestions = 10)
        dao.insert(score)
        
        val retrieved = dao.getTopScores(1)
        
        assertEquals(1, retrieved.size)
        assertEquals(100, retrieved[0].score)
    }
}
```

### UI Tests

**Compose UI Tests**:
```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun homeScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            HomeScreen(
                viewModel = FakeHomeViewModel(),
                onNavigate = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Math Workout")
            .assertIsDisplayed()
    }
}
```

---

## Build Configuration

### Gradle Files

**Project-level build.gradle.kts**:
```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

**App-level build.gradle.kts**:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.athreya.mathworkout"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.athreya.mathworkout"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}
```

---

## Future Enhancements

### Planned Features

1. **Enhanced Social**:
   - Real-time multiplayer battles
   - Voice chat in groups
   - Tournaments and competitions

2. **Advanced Learning**:
   - AI-powered difficulty adjustment
   - Personalized learning paths
   - Detailed analytics and insights

3. **Gamification**:
   - More achievement types
   - Seasonal events
   - Limited-time challenges

4. **Platform Expansion**:
   - iOS version
   - Web version
   - Wear OS support
   - Tablet optimization

5. **Accessibility**:
   - Screen reader support
   - High contrast modes
   - Font size customization
   - Voice commands

---

## Conclusion

Athreya's Math Workout demonstrates modern Android development practices using Jetpack Compose, MVVM architecture, and Firebase integration. The codebase is designed for:

- **Maintainability**: Clear separation of concerns
- **Testability**: Dependency injection and abstraction
- **Scalability**: Modular architecture
- **Performance**: Optimized rendering and data access
- **User Experience**: Smooth animations and responsive UI

The architecture supports future growth while maintaining code quality and development velocity.

---

**Document Version**: 1.0  
**Last Updated**: November 16, 2024  
**Author**: Harikrishnan Raguraman
