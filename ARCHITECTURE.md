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
11. [Achievement & Badge System](#achievement--badge-system)
12. [Interactive Games System](#interactive-games-system)
13. [Smart Hint System](#smart-hint-system)
14. [Social Features](#social-features)
15. [Security & Privacy](#security--privacy)
16. [Performance Optimizations](#performance-optimizations)
17. [Testing Strategy](#testing-strategy)

---

## Overview

Athreya's Math Workout is a modern Android application built using **MVVM (Model-View-ViewModel)** architecture with **Jetpack Compose** for UI. The app follows clean architecture principles, separating concerns into distinct layers for maintainability, testability, and scalability.

The application features:
- **Core Math Games**: Addition, Subtraction, Multiplication, Division, Mixed, Brain Teasers
- **Interactive Games**: Sudoku, Math Tricks Library, Daily Riddles
- **Achievement System**: 50+ badges across 5 categories with 5 rarity levels
- **Social Features**: Groups, Challenges, Global Leaderboards
- **Educational Content**: Famous Mathematicians, Math Tricks with practice mode
- **Smart Learning**: Progressive hint system with educational explanations
- **Gamification**: Daily challenges, streak multipliers, rank progression

### Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | Jetpack Compose | Declarative UI framework |
| **Architecture** | MVVM | Separation of concerns |
| **Navigation** | Compose Navigation | Screen routing (20+ screens) |
| **State Management** | StateFlow, MutableState | Reactive state handling |
| **Local Database** | Room (v9) | Persistent local storage |
| **Remote Database** | Firebase Firestore | Cloud data sync |
| **Authentication** | Firebase Auth | User identity |
| **Cloud Messaging** | Firebase FCM | Push notifications |
| **Settings** | SharedPreferences, DataStore | User preferences |
| **Dependency Injection** | Manual (ViewModelFactory) | Dependency management |
| **Asynchronous** | Kotlin Coroutines | Background operations |
| **Language** | Kotlin 1.9.0 | Main programming language |
| **UI Toolkit** | Material Design 3 | Modern Material components |

### Design Principles

1. **Separation of Concerns**: Each layer has a single, well-defined responsibility
2. **Unidirectional Data Flow**: Data flows from ViewModel → UI, events flow UI → ViewModel
3. **Single Source of Truth**: ViewModels hold the UI state
4. **Immutability**: Use of data classes and StateFlow for predictable state
5. **Reactive Programming**: StateFlow and Compose state for reactive UI updates
6. **Dependency Inversion**: High-level modules don't depend on low-level modules
7. **Progressive Enhancement**: Features unlock based on user progress
8. **Educational First**: Learning prioritized over pure competition

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

## Achievement & Badge System

The app features a comprehensive achievement and badge system that rewards players for various accomplishments across gameplay, consistency, and skill mastery.

### Badge Data Model

**File**: `data/Badge.kt`

The badge system includes **50+ unique badges** across **5 categories** with **5 rarity levels**.

```kotlin
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val icon: String,
    val requirement: String
)

enum class BadgeCategory {
    SPEED,      // Time-based achievements
    ACCURACY,   // Precision-based achievements
    COLLECTION, // Completionist achievements
    CHALLENGE,  // Social and competitive achievements
    DEDICATION  // Consistency and effort achievements
}

enum class BadgeRarity(val displayName: String, val color: Color) {
    BRONZE("Bronze", Color(0xFFCD7F32)),
    SILVER("Silver", Color(0xFFC0C0C0)),
    GOLD("Gold", Color(0xFFFFD700)),
    PLATINUM("Platinum", Color(0xFFE5E4E2)),
    DIAMOND("Diamond", Color(0xFFB9F2FF))
}

object Badges {
    // Speed Category Examples
    val SPEED_DEMON = Badge(
        id = "speed_demon",
        name = "Speed Demon",
        description = "Complete 10 games in Quick mode",
        category = BadgeCategory.SPEED,
        rarity = BadgeRarity.BRONZE,
        icon = "⚡",
        requirement = "10 quick games"
    )
    
    val LIGHTNING_FAST = Badge(
        id = "lightning_fast",
        name = "Lightning Fast",
        description = "Complete a game in under 60 seconds",
        category = BadgeCategory.SPEED,
        rarity = BadgeRarity.GOLD,
        icon = "⚡",
        requirement = "Game < 60s"
    )
    
    // Accuracy Category Examples
    val SHARPSHOOTER = Badge(
        id = "sharpshooter",
        name = "Sharpshooter",
        description = "Get 10 answers correct in a row",
        category = BadgeCategory.ACCURACY,
        rarity = BadgeRarity.SILVER,
        icon = "🎯",
        requirement = "10 correct streak"
    )
    
    val PERFECTIONIST = Badge(
        id = "perfectionist",
        name = "Perfectionist",
        description = "Complete 5 games with 100% accuracy",
        category = BadgeCategory.ACCURACY,
        rarity = BadgeRarity.GOLD,
        icon = "💯",
        requirement = "5 perfect games"
    )
    
    // Dedication Category Examples
    val DAILY_WARRIOR = Badge(
        id = "daily_warrior",
        name = "Daily Warrior",
        description = "Complete daily challenge 7 days in a row",
        category = BadgeCategory.DEDICATION,
        rarity = BadgeRarity.SILVER,
        icon = "🔥",
        requirement = "7-day streak"
    )
    
    val MONTH_MASTER = Badge(
        id = "month_master",
        name = "Month Master",
        description = "Complete daily challenge 30 days in a row",
        category = BadgeCategory.DEDICATION,
        rarity = BadgeRarity.DIAMOND,
        icon = "🏆",
        requirement = "30-day streak"
    )
    
    // Challenge Category Examples
    val CHALLENGER = Badge(
        id = "challenger",
        name = "Challenger",
        description = "Win your first player challenge",
        category = BadgeCategory.CHALLENGE,
        rarity = BadgeRarity.BRONZE,
        icon = "⚔️",
        requirement = "1 challenge win"
    )
    
    val CHAMPION = Badge(
        id = "champion",
        name = "Champion",
        description = "Win 50 player challenges",
        category = BadgeCategory.CHALLENGE,
        rarity = BadgeRarity.PLATINUM,
        icon = "👑",
        requirement = "50 challenge wins"
    )
    
    // Collection Category Examples
    val GAME_EXPLORER = Badge(
        id = "game_explorer",
        name = "Game Explorer",
        description = "Try all 6 game modes",
        category = BadgeCategory.COLLECTION,
        rarity = BadgeRarity.BRONZE,
        icon = "🧭",
        requirement = "All modes played"
    )
    
    val BADGE_COLLECTOR = Badge(
        id = "badge_collector",
        name = "Badge Collector",
        description = "Unlock 25 different badges",
        category = BadgeCategory.COLLECTION,
        rarity = BadgeRarity.GOLD,
        icon = "📌",
        requirement = "25 badges"
    )
    
    fun getAllBadges(): List<Badge> = listOf(
        // Returns all 50+ badges
    )
}
```

### Badge Manager

**File**: `data/BadgeManager.kt`

The BadgeManager handles badge unlocking logic and progress tracking.

```kotlin
class BadgeManager(private val userPreferences: UserPreferencesManager) {
    
    fun getUnlockedBadges(): List<Badge> {
        val unlockedIds = userPreferences.getUnlockedBadgeIds()
        return Badges.getAllBadges().filter { it.id in unlockedIds }
    }
    
    fun getAllBadges(): List<Badge> = Badges.getAllBadges()
    
    fun getBadgeProgress(badgeId: String): BadgeProgress {
        return when (badgeId) {
            "speed_demon" -> {
                val quickGames = userPreferences.getQuickGamesPlayed()
                BadgeProgress(
                    current = quickGames,
                    required = 10,
                    unlocked = quickGames >= 10
                )
            }
            "daily_warrior" -> {
                val streak = userPreferences.getCurrentStreak()
                BadgeProgress(
                    current = streak,
                    required = 7,
                    unlocked = streak >= 7
                )
            }
            // ... other badge progress calculations
            else -> BadgeProgress(0, 0, false)
        }
    }
    
    fun updateBadgeProgress(gameStats: GameStats): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        
        // Check all badges for unlock conditions
        Badges.getAllBadges().forEach { badge ->
            if (!isBadgeUnlocked(badge.id)) {
                if (checkBadgeCondition(badge, gameStats)) {
                    unlockBadge(badge.id)
                    newlyUnlocked.add(badge)
                }
            }
        }
        
        return newlyUnlocked
    }
    
    private fun checkBadgeCondition(badge: Badge, stats: GameStats): Boolean {
        return when (badge.id) {
            "speed_demon" -> stats.quickGamesPlayed >= 10
            "sharpshooter" -> stats.correctStreakCurrent >= 10
            "perfectionist" -> stats.perfectGamesCount >= 5
            "daily_warrior" -> stats.dailyStreak >= 7
            "challenger" -> stats.challengeWins >= 1
            // ... all badge conditions
            else -> false
        }
    }
    
    private fun unlockBadge(badgeId: String) {
        val currentBadges = userPreferences.getUnlockedBadgeIds().toMutableSet()
        currentBadges.add(badgeId)
        userPreferences.saveUnlockedBadgeIds(currentBadges.toList())
    }
    
    private fun isBadgeUnlocked(badgeId: String): Boolean {
        return badgeId in userPreferences.getUnlockedBadgeIds()
    }
}

data class BadgeProgress(
    val current: Int,
    val required: Int,
    val unlocked: Boolean
)
```

### Badge UI Components

**File**: `ui/components/BadgeDisplay.kt`

Reusable UI components for displaying badges across multiple screens.

```kotlin
// Badge Card - Full detailed view
@Composable
fun BadgeCard(
    badge: Badge,
    isUnlocked: Boolean,
    progress: BadgeProgress?,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                badge.rarity.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge icon
                Text(
                    text = badge.icon,
                    fontSize = 40.sp,
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = badge.rarity.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = badge.rarity.color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Progress bar if not unlocked
            if (!isUnlocked && progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress.current.toFloat() / progress.required,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${progress.current} / ${progress.required}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Badge Grid - Grid layout for collections
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
            BadgeIndicator(
                badge = badge,
                isUnlocked = isUnlocked,
                onClick = { onBadgeClick(badge) }
            )
        }
    }
}

// Badge Indicator - Compact badge icon with rarity border
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

// Badge Row - Horizontal row with overflow indicator
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
        badges.take(maxBadges).forEach { badge ->
            BadgeIndicator(
                badge = badge,
                isUnlocked = true
            )
        }
        
        // Show "+X" indicator if more badges
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

// Badge Detail Dialog - Full-screen dialog with badge details
@Composable
fun BadgeDetailDialog(
    badge: Badge,
    isUnlocked: Boolean,
    progress: BadgeProgress?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.icon,
                    fontSize = 64.sp,
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = badge.name)
            }
        },
        text = {
            Column {
                // Rarity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = badge.rarity.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = badge.rarity.displayName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = badge.rarity.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(text = badge.description)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Requirement
                Text(
                    text = "Requirement: ${badge.requirement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Progress
                if (!isUnlocked && progress != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = progress.current.toFloat() / progress.required,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Progress: ${progress.current} / ${progress.required}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
```

### Badge Integration Across Screens

**1. Home Screen**
```kotlin
// File: ui/screens/HomeScreen.kt
Column {
    // Username and rank
    Text(text = "Welcome, ${uiState.playerName}")
    Text(text = "${uiState.rank.icon} ${uiState.rank.name}")
    
    // Badge display
    if (uiState.unlockedBadges.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        BadgeRow(
            badges = uiState.unlockedBadges,
            maxBadges = 5
        )
    }
}
```

**2. Global Leaderboard**
```kotlin
// File: ui/components/GlobalLeaderboardScreen.kt
Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = player.name)
    
    Spacer(modifier = Modifier.width(8.dp))
    
    // Show top 3 badges
    val badges = remember { badgeManager.getUnlockedBadges().take(3) }
    BadgeRow(badges = badges, maxBadges = 3)
}
```

**3. Group Leaderboard**
```kotlin
// File: ui/screens/GroupDetailScreen.kt
if (isCurrentUser && unlockedBadges.isNotEmpty()) {
    Spacer(modifier = Modifier.height(4.dp))
    BadgeRow(
        badges = unlockedBadges,
        maxBadges = 3,
        modifier = Modifier.padding(start = 8.dp)
    )
}
```

**4. Challenges**
```kotlin
// File: ui/screens/ChallengesScreen.kt
Column {
    Text(text = "${challenge.challengerName} challenges you!")
    
    // Challenger's badges
    val challengerBadges = remember { 
        badgeManager.getUserBadges(challenge.challengerId).take(3)
    }
    if (challengerBadges.isNotEmpty()) {
        BadgeRow(badges = challengerBadges, maxBadges = 3)
    }
}
```

**5. Badges Screen**
```kotlin
// File: ui/screens/BadgesScreen.kt
@Composable
fun BadgesScreen(viewModel: BadgeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // Category tabs
        TabRow(selectedTabIndex = uiState.selectedCategory) {
            BadgeCategory.values().forEach { category ->
                Tab(
                    selected = uiState.selectedCategory == category.ordinal,
                    onClick = { viewModel.selectCategory(category) },
                    text = { Text(category.name) }
                )
            }
        }
        
        // Badge grid
        BadgeGrid(
            badges = uiState.filteredBadges,
            unlockedBadgeIds = uiState.unlockedBadgeIds,
            onBadgeClick = { viewModel.showBadgeDetail(it) }
        )
    }
    
    // Detail dialog
    uiState.selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            isUnlocked = badge.id in uiState.unlockedBadgeIds,
            progress = viewModel.getBadgeProgress(badge.id),
            onDismiss = { viewModel.dismissBadgeDetail() }
        )
    }
}
```

### Achievement Data Model

**File**: `data/Achievement.kt`

The original achievement system tracks milestones and unlocks themes.

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

## Interactive Games System

The app includes three interactive learning games beyond traditional math practice.

### Sudoku Game

**File**: `ui/screens/SudokuScreen.kt`

A complete Sudoku implementation with puzzle generation, validation, and scoring.

```kotlin
@Composable
fun SudokuScreen(viewModel: SudokuViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // Header with difficulty and timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Difficulty: ${uiState.difficulty}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Time: ${formatTime(uiState.elapsedTime)}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // 9x9 Sudoku grid
        SudokuGrid(
            board = uiState.board,
            solution = uiState.solution,
            selectedCell = uiState.selectedCell,
            conflicts = uiState.conflicts,
            onCellSelected = { row, col -> 
                viewModel.selectCell(row, col) 
            }
        )
        
        // Number pad
        NumberPad(
            onNumberSelected = { number -> 
                viewModel.enterNumber(number) 
            },
            onClear = { viewModel.clearCell() },
            onHint = { viewModel.useHint() }
        )
        
        // Completion dialog
        if (uiState.isCompleted) {
            CompletionDialog(
                time = uiState.elapsedTime,
                score = uiState.score,
                onNewGame = { viewModel.startNewGame() },
                onDismiss = { /* navigate back */ }
            )
        }
    }
}
```

**Sudoku Engine**: `game/SudokuEngine.kt`

```kotlin
class SudokuEngine {
    
    fun generatePuzzle(difficulty: Difficulty): SudokuPuzzle {
        val solution = generateSolution()
        val puzzle = removeNumbers(solution, difficulty.cellsToRemove)
        
        return SudokuPuzzle(
            board = puzzle,
            solution = solution,
            difficulty = difficulty
        )
    }
    
    private fun generateSolution(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        solveSudoku(board, 0, 0)
        return board
    }
    
    private fun solveSudoku(
        board: Array<IntArray>, 
        row: Int, 
        col: Int
    ): Boolean {
        if (row == 9) return true
        if (col == 9) return solveSudoku(board, row + 1, 0)
        if (board[row][col] != 0) return solveSudoku(board, row, col + 1)
        
        val numbers = (1..9).shuffled()
        for (num in numbers) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                if (solveSudoku(board, row, col + 1)) return true
                board[row][col] = 0
            }
        }
        return false
    }
    
    fun isValid(
        board: Array<IntArray>, 
        row: Int, 
        col: Int, 
        num: Int
    ): Boolean {
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
    
    fun findConflicts(board: Array<IntArray>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()
        
        for (row in 0..8) {
            for (col in 0..8) {
                val num = board[row][col]
                if (num != 0) {
                    if (!isValidPlacement(board, row, col)) {
                        conflicts.add(Pair(row, col))
                    }
                }
            }
        }
        
        return conflicts
    }
}

data class SudokuPuzzle(
    val board: Array<IntArray>,
    val solution: Array<IntArray>,
    val difficulty: Difficulty
)

enum class Difficulty(val cellsToRemove: Int, val scoreMultiplier: Float) {
    EASY(30, 1.0f),
    MEDIUM(45, 1.5f),
    HARD(55, 2.0f),
    EXPERT(64, 3.0f)
}
```

### Math Tricks Library

**File**: `ui/screens/MathTricksScreen.kt`

Educational content teaching mental math shortcuts with practice mode.

```kotlin
@Composable
fun MathTricksScreen(viewModel: MathTricksViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // List of tricks
        LazyColumn {
            items(uiState.mathTricks) { trick ->
                MathTrickCard(
                    trick = trick,
                    onLearnMore = { viewModel.selectTrick(trick) },
                    onPractice = { viewModel.startPractice(trick) }
                )
            }
        }
    }
    
    // Detail view
    uiState.selectedTrick?.let { trick ->
        MathTrickDetailDialog(
            trick = trick,
            onDismiss = { viewModel.clearSelection() },
            onPractice = { viewModel.startPractice(trick) }
        )
    }
}

@Composable
fun MathTrickCard(
    trick: MathTrick,
    onLearnMore: () -> Unit,
    onPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = trick.title,
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = trick.shortDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row {
                TextButton(onClick = onLearnMore) {
                    Text("Learn More")
                }
                TextButton(onClick = onPractice) {
                    Text("Practice")
                }
            }
        }
    }
}
```

**Math Trick Model**: `data/MathTrick.kt`

```kotlin
data class MathTrick(
    val id: String,
    val title: String,
    val shortDescription: String,
    val fullExplanation: String,
    val steps: List<String>,
    val examples: List<Example>,
    val practiceQuestions: List<Question>
) {
    data class Example(
        val problem: String,
        val solution: String,
        val explanation: String
    )
}

object MathTricks {
    val MULTIPLY_BY_11 = MathTrick(
        id = "multiply_11",
        title = "Multiply by 11",
        shortDescription = "Quick way to multiply two-digit numbers by 11",
        fullExplanation = """
            To multiply any two-digit number by 11:
            1. Add the two digits
            2. Place the sum between the two digits
            3. If sum > 9, carry the 1
        """.trimIndent(),
        steps = listOf(
            "Take the two digits (e.g., 23)",
            "Add them together (2 + 3 = 5)",
            "Place sum between digits (2_5_3 = 253)"
        ),
        examples = listOf(
            MathTrick.Example(
                problem = "23 × 11",
                solution = "253",
                explanation = "2 and 3, sum is 5, place between: 253"
            ),
            MathTrick.Example(
                problem = "47 × 11",
                solution = "517",
                explanation = "4 and 7, sum is 11, carry 1: 4(1+1)7 = 517"
            )
        ),
        practiceQuestions = listOf(
            Question("34 × 11", 374),
            Question("56 × 11", 616),
            Question("89 × 11", 979)
        )
    )
    
    val SQUARE_NUMBERS_ENDING_5 = MathTrick(
        id = "square_ending_5",
        title = "Square Numbers Ending in 5",
        shortDescription = "Instantly square any number ending in 5",
        fullExplanation = """
            For any number ending in 5:
            1. Take the first digit(s)
            2. Multiply by the next higher number
            3. Append 25 to the result
        """.trimIndent(),
        steps = listOf(
            "Example: 35²",
            "First digit is 3, next higher is 4",
            "3 × 4 = 12",
            "Append 25: 1225"
        ),
        examples = listOf(
            MathTrick.Example(
                problem = "25²",
                solution = "625",
                explanation = "2 × 3 = 6, append 25 = 625"
            ),
            MathTrick.Example(
                problem = "75²",
                solution = "5625",
                explanation = "7 × 8 = 56, append 25 = 5625"
            )
        ),
        practiceQuestions = listOf(
            Question("45²", 2025),
            Question("85²", 7225),
            Question("95²", 9025)
        )
    )
    
    fun getAllTricks() = listOf(
        MULTIPLY_BY_11,
        SQUARE_NUMBERS_ENDING_5,
        // ... more tricks
    )
}
```

### Daily Riddle

**File**: `ui/screens/DailyRiddleScreen.kt`

Daily brain teasers with hints and explanations.

```kotlin
@Composable
fun DailyRiddleScreen(viewModel: DailyRiddleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Date header
        Text(
            text = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(Date()),
            style = MaterialTheme.typography.titleLarge
        )
        
        // Riddle card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = uiState.riddle.question,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Answer input
                if (!uiState.isAnswered) {
                    OutlinedTextField(
                        value = uiState.userAnswer,
                        onValueChange = { viewModel.updateAnswer(it) },
                        label = { Text("Your answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Button(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit")
                    }
                    
                    // Hint button
                    if (!uiState.hintShown) {
                        TextButton(onClick = { viewModel.showHint() }) {
                            Text("Show Hint")
                        }
                    } else {
                        Text(
                            text = "Hint: ${uiState.riddle.hint}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    // Show answer and explanation
                    Text(
                        text = if (uiState.isCorrect) "Correct! ✓" else "Not quite!",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState.isCorrect) 
                            Color.Green else Color.Red
                    )
                    
                    Text(
                        text = "Answer: ${uiState.riddle.answer}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = uiState.riddle.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
```

**Riddle Model**: `data/Riddle.kt`

```kotlin
data class Riddle(
    val id: String,
    val date: String,
    val question: String,
    val answer: String,
    val hint: String,
    val explanation: String,
    val category: RiddleCategory
)

enum class RiddleCategory {
    LOGIC,
    MATH,
    PATTERN,
    WORD_PLAY
}

object Riddles {
    fun getRiddleForDate(date: String): Riddle {
        // Pseudo-random based on date to ensure consistency
        val riddles = getAllRiddles()
        val index = date.hashCode().absoluteValue % riddles.size
        return riddles[index].copy(date = date)
    }
    
    private fun getAllRiddles() = listOf(
        Riddle(
            id = "riddle_1",
            date = "",
            question = "If you have 5 apples and take away 3, how many do you have?",
            answer = "3",
            hint = "Think about what 'take away' means",
            explanation = "You have 3 apples because you took them!",
            category = RiddleCategory.LOGIC
        ),
        Riddle(
            id = "riddle_2",
            question = "What is the next number: 2, 6, 12, 20, 30, __?",
            answer = "42",
            hint = "Look at the differences between numbers",
            explanation = "Pattern: +4, +6, +8, +10, +12. Next is 30 + 12 = 42",
            category = RiddleCategory.PATTERN
        ),
        // ... 365+ riddles for year-round content
    )
}
```

---

## Smart Hint System

The app features an educational hint system that provides progressive help without giving away answers immediately.

### Per-Question Attempt Tracking

**File**: `viewmodel/GameViewModel.kt`

```kotlin
class GameViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    private val questionAttempts = mutableMapOf<Int, Int>()
    
    private val _currentHintState = MutableStateFlow<HintState>(HintState.None)
    val currentHintState: StateFlow<HintState> = _currentHintState.asStateFlow()
    
    fun checkAnswer(answer: Int) {
        val currentQuestion = _currentQuestionIndex.value
        val attempts = questionAttempts.getOrDefault(currentQuestion, 0)
        
        if (answer == currentQuestion.correctAnswer) {
            // Correct answer
            handleCorrectAnswer()
            resetHintState()
        } else {
            // Wrong answer - increment attempts
            val newAttempts = attempts + 1
            questionAttempts[currentQuestion] = newAttempts
            
            // Update hint state based on attempts
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
                    // Third wrong: show detailed hint
                    _currentHintState.value = HintState.ShowDetailedHint(
                        currentQuestion.hint,
                        currentQuestion.detailedExplanation
                    )
                }
                else -> {
                    // Fourth+ wrong: show answer and move on
                    _currentHintState.value = HintState.ShowAnswer(
                        currentQuestion.correctAnswer,
                        currentQuestion.fullExplanation
                    )
                    // Auto-advance after showing answer
                    viewModelScope.launch {
                        delay(3000)
                        moveToNextQuestion()
                    }
                }
            }
        }
    }
    
    private fun resetHintState() {
        _currentHintState.value = HintState.None
        questionAttempts.remove(_currentQuestionIndex.value)
    }
}

sealed class HintState {
    object None : HintState()
    object FirstAttempt : HintState()
    data class ShowHint(val hint: String) : HintState()
    data class ShowDetailedHint(
        val hint: String, 
        val explanation: String
    ) : HintState()
    data class ShowAnswer(
        val answer: Int, 
        val fullExplanation: String
    ) : HintState()
}
```

### Question Model with Educational Content

**File**: `game/QuestionGenerator.kt`

```kotlin
data class Question(
    val text: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val difficulty: Int,
    val hint: String,
    val detailedExplanation: String,
    val fullExplanation: String
)

object QuestionGenerator {
    
    fun generateQuestion(
        gameType: GameType,
        difficulty: Int
    ): Question {
        return when (gameType) {
            GameType.ADDITION -> generateAdditionQuestion(difficulty)
            GameType.SUBTRACTION -> generateSubtractionQuestion(difficulty)
            GameType.MULTIPLICATION -> generateMultiplicationQuestion(difficulty)
            GameType.DIVISION -> generateDivisionQuestion(difficulty)
            GameType.MIXED -> generateMixedQuestion(difficulty)
            GameType.BRAIN_TEASER -> generateBrainTeaser(difficulty)
        }
    }
    
    private fun generateAdditionQuestion(difficulty: Int): Question {
        val num1 = getRandomNumber(difficulty)
        val num2 = getRandomNumber(difficulty)
        val answer = num1 + num2
        
        return Question(
            text = "$num1 + $num2 = ?",
            correctAnswer = answer,
            options = generateOptions(answer),
            difficulty = difficulty,
            hint = "Try adding ${num1} and ${num2} step by step",
            detailedExplanation = """
                Break it down:
                1. Start with $num1
                2. Add $num2 to it
                3. Result: ${num1} + ${num2} = $answer
            """.trimIndent(),
            fullExplanation = """
                Addition: $num1 + $num2
                
                Step-by-step:
                • We're combining two numbers
                • $num1 + $num2 = $answer
                
                Tip: For larger numbers, try breaking them into 
                tens and ones for easier mental math!
            """.trimIndent()
        )
    }
    
    private fun generateMultiplicationQuestion(difficulty: Int): Question {
        val num1 = getRandomNumber(difficulty)
        val num2 = getRandomNumber(difficulty)
        val answer = num1 * num2
        
        return Question(
            text = "$num1 × $num2 = ?",
            correctAnswer = answer,
            options = generateOptions(answer),
            difficulty = difficulty,
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
                
                Tip: Memorizing multiplication tables makes 
                this much faster!
            """.trimIndent()
        )
    }
    
    private fun generateBrainTeaser(difficulty: Int): Question {
        val teasers = listOf(
            Question(
                text = "If 3 cats can catch 3 mice in 3 minutes, how many cats are needed to catch 100 mice in 100 minutes?",
                correctAnswer = 3,
                options = listOf(1, 3, 33, 100),
                difficulty = 3,
                hint = "Think about the rate - cats per mice per time",
                detailedExplanation = """
                    Each cat catches 1 mouse in 3 minutes.
                    So each cat catches 100/3 ≈ 33.33 mice in 100 minutes.
                    You still need 3 cats!
                """.trimIndent(),
                fullExplanation = """
                    This is a rate problem!
                    
                    Analysis:
                    • 3 cats catch 3 mice in 3 minutes
                    • That means 1 cat catches 1 mouse in 3 minutes
                    • In 100 minutes, 1 cat catches 100/3 mice
                    • To catch 100 mice, you need 3 cats
                    
                    The time increased proportionally, so you 
                    need the same number of cats!
                """.trimIndent()
            ),
            // ... more brain teasers
        )
        
        return teasers[Random.nextInt(teasers.size)]
    }
}
```

### Hint Display UI

**File**: `ui/screens/GameScreen.kt`

```kotlin
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
                    color = Color.Red
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
                    Text(
                        text = "💡 Hint:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )
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
                    Text(
                        text = "💡 Detailed Hint:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Orange
                    )
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
                    Text(
                        text = "✓ Answer: ${hintState.answer}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Green
                    )
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
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
```

### Learning Benefits

The smart hint system provides:

1. **Progressive Support**: Hints escalate from encouragement → hint → detailed explanation → full answer
2. **Attempt Tracking**: Each question independently tracked (no global hint limit)
3. **Educational Focus**: Full explanations teach concepts, not just answers
4. **Auto-Progression**: After 4 attempts, answer shown and game continues (prevents frustration)
5. **Visual Feedback**: Color-coded cards (Red → Blue → Orange → Green) indicate help level
6. **No Penalties**: Hints don't reduce score, encouraging learning over guessing

---

## Social Features

The app includes robust social and competitive features.

### Groups System

**File**: `data/Group.kt`

````

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
