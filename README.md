# Athreya's Sums - Math Workout Game

A modern Android math workout game built with **Kotlin**, **Jetpack Compose**, and **MVVM architecture**. Features both mathematical problem-solving and logic puzzles including a complete **Sudoku game mode**. This app is designed as a learning project for Android development newcomers, featuring comprehensive comments and modern Android development practices.

## 🎯 Features

- **Five Game Modes:**
  - Addition & Subtraction
  - Multiplication & Division
  - Test Me (Mixed operations)
  - Brain Teaser (Multi-step problems)
  - 🧩 **Sudoku Puzzles** (NEW!)

- **Sudoku Features:**
  - Auto-generated puzzles with multiple difficulty levels
  - Note-taking functionality for solving strategies
  - Undo/Redo system and hint system
  - Real-time progress tracking and timer
  - Professional grid interface with conflict detection

- **Math Game Features:**
  - Three Difficulty Levels: Easy (1-10), Medium (1-100), Complex (1-1000)
  - Customizable question count (10, 20, 50)
  - Real-time timer and scoring
  - Wrong answer penalty system
  - High score tracking with Room database
  - Persistent settings with DataStore

## 🏗️ Architecture

This app follows **Model-View-ViewModel (MVVM)** architecture with modern Android development practices:

### Technology Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (100% declarative UI)
- **Architecture:** MVVM with Repository pattern
- **Database:** Room (SQLite abstraction)
- **Settings:** DataStore (modern SharedPreferences replacement)
- **Navigation:** Compose Navigation
- **State Management:** StateFlow and Compose State
- **Dependency Injection:** Manual DI (production apps use Dagger/Hilt)

### Project Structure
```
app/src/main/java/com/athreya/mathworkout/
├── data/                          # Data layer
│   ├── AppDatabase.kt             # Room database setup
│   ├── GameSettings.kt            # Settings data classes & enums
│   ├── HighScore.kt              # High score entity
│   ├── HighScoreDao.kt           # Database access object
│   └── SettingsManager.kt        # DataStore settings manager
├── game/                          # Game logic
│   └── QuestionGenerator.kt       # Math question generation
├── navigation/                    # Navigation setup
│   └── Screen.kt                 # Route definitions
├── ui/
│   ├── screens/                  # All Compose screens
│   │   ├── HomeScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── GameScreen.kt
│   │   ├── ResultsScreen.kt
│   │   └── HighScoreScreen.kt
│   └── theme/                    # Material Design 3 theme
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/                    # ViewModels for state management
│   ├── SettingsViewModel.kt
│   ├── GameViewModel.kt
│   └── HighScoreViewModel.kt
└── MainActivity.kt               # Single activity + navigation setup
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Hedgehog or later)
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Kotlin:** 1.9.10+

### Installation
1. **Clone or download** this project
2. **Open in Android Studio**
3. **Sync Gradle** files (should happen automatically)
4. **Run the app** on device or emulator

### Building the App
```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## 📱 App Flow

### 1. Home Screen
- Main menu with four game mode buttons
- Settings and High Scores access
- Material Design 3 styling

### 2. Settings Screen
- Difficulty selection (Easy/Medium/Complex)
- Question count selection (10/20/50) 
- Settings persist using DataStore

### 3. Game Screen
- Real-time question display
- Progress indicator
- Timer tracking
- Wrong attempt counter
- Keyboard handling for number input

### 4. Results Screen
- Game time breakdown
- Penalty calculations (5 seconds per wrong answer)
- New record detection
- Auto-save to high scores database

### 5. High Scores Screen
- Filterable high scores list
- Sort by game mode or difficulty
- Ranking with emoji medals (🏆🥈🥉)
- Date and performance tracking

## 🏛️ Key Android Concepts Demonstrated

### Jetpack Compose
```kotlin
@Composable
fun HomeScreen(
    onGameModeSelected: (GameMode) -> Unit,
    onSettingsClick: () -> Unit,
    onHighScoresClick: () -> Unit
) {
    // Declarative UI - describe what you want, not how to build it
    Column {
        Text("Math Workout")
        Button(onClick = { onGameModeSelected(GameMode.ADDITION_SUBTRACTION) }) {
            Text("Addition & Subtraction")
        }
    }
}
```

### State Management with ViewModels
```kotlin
class GameViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    fun updateUserAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(userAnswer = answer)
    }
}
```

### Room Database
```kotlin
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String,
    val difficulty: String,
    val timeTaken: Long
)
```

### DataStore Settings
```kotlin
class SettingsManager(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("game_settings")
    
    val gameSettings: Flow<GameSettings> = context.dataStore.data.map { preferences ->
        GameSettings(
            difficulty = Difficulty.valueOf(preferences[DIFFICULTY_KEY] ?: "EASY"),
            questionCount = preferences[QUESTION_COUNT_KEY] ?: 10
        )
    }
}
```

### Navigation with Compose
```kotlin
NavHost(navController = navController, startDestination = "home") {
    composable("home") { 
        HomeScreen(onGameModeSelected = { mode ->
            navController.navigate("game/$mode")
        })
    }
    composable("game/{gameMode}") { backStackEntry ->
        val gameMode = backStackEntry.arguments?.getString("gameMode")
        GameScreen(gameMode = GameMode.valueOf(gameMode))
    }
}
```

## 🎮 Game Logic

### Question Generation
The app generates different types of math problems based on game mode and difficulty:

```kotlin
fun generateQuestion(gameMode: GameMode, difficulty: Difficulty): MathQuestion {
    val range = when (difficulty) {
        Difficulty.EASY -> 10
        Difficulty.MEDIUM -> 100  
        Difficulty.COMPLEX -> 1000
    }
    
    return when (gameMode) {
        GameMode.ADDITION_SUBTRACTION -> generateAddSubtraction(range)
        GameMode.BRAIN_TEASER -> generateMultiStep(range)
        // ... other modes
    }
}
```

### Scoring System
- **Base Score:** Actual time taken to complete all questions
- **Penalty:** 5 seconds added for each wrong answer
- **Final Score:** Base time + penalties
- **High Scores:** Sorted by lowest final score (fastest time)

## 🔧 Customization

### Adding New Game Modes
1. Add to `GameMode` enum in `GameSettings.kt`
2. Update `QuestionGenerator.kt` with new question types
3. Add UI button in `HomeScreen.kt`
4. Update navigation in `MainActivity.kt`

### Changing Difficulty Ranges
Modify the `getNumberRange()` function in `QuestionGenerator.kt`:

```kotlin
private fun getNumberRange(difficulty: Difficulty): Int {
    return when (difficulty) {
        Difficulty.EASY -> 20      // Changed from 10
        Difficulty.MEDIUM -> 200   // Changed from 100  
        Difficulty.COMPLEX -> 2000 // Changed from 1000
    }
}
```

### Adding New Question Types
Extend `QuestionGenerator.kt` with new mathematical operations:

```kotlin
private fun generateFractions(difficulty: Difficulty): MathQuestion {
    // Your custom fraction questions
}
```

## 📊 Database Schema

### HighScore Table
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary key (auto-generated) |
| gameMode | String | Game mode played |
| difficulty | String | Difficulty level |
| timeTaken | Long | Total time in milliseconds |
| wrongAttempts | Int | Number of wrong answers |
| timestamp | Long | When score was achieved |

### Settings (DataStore)
| Key | Type | Description |
|-----|------|-------------|
| difficulty | String | Current difficulty setting |
| question_count | Int | Number of questions per game |

## 🎨 UI Design

The app follows **Material Design 3** guidelines:
- **Color System:** Dynamic color support (Android 12+)
- **Typography:** Material 3 type scale
- **Components:** Cards, Buttons, TextFields with proper elevation
- **Navigation:** Bottom-up navigation patterns
- **Accessibility:** Proper content descriptions and semantic roles

## 🧪 Testing

The project structure supports easy testing:

```kotlin
// Example ViewModel test
@Test
fun `submitAnswer with correct answer moves to next question`() {
    val viewModel = GameViewModel(mockSettingsManager)
    viewModel.initializeGame(GameMode.ADDITION_SUBTRACTION)
    viewModel.updateUserAnswer("10") // Assuming correct answer
    viewModel.submitAnswer()
    
    val state = viewModel.uiState.value
    assertEquals(2, state.questionNumber)
}
```

## 🚀 Next Steps

Ideas for extending this project:
- **Multiplayer mode** with Firebase
- **Achievements system** 
- **Statistics dashboard** with charts
- **Custom question sets**
- **Voice recognition** for answers
- **Accessibility improvements**
- **Widget support** for quick games
- **Export scores** to CSV/JSON

## 📚 Learning Resources

This project demonstrates many Android concepts. To learn more:

- **Jetpack Compose:** [Official Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- **Room Database:** [Room Guide](https://developer.android.com/training/data-storage/room)
- **MVVM Architecture:** [Android Architecture Guide](https://developer.android.com/guide/framework/architecture)
- **DataStore:** [DataStore Guide](https://developer.android.com/topic/libraries/architecture/datastore)
- **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

## 🤝 Contributing

This is a learning project! Feel free to:
- Add new question types
- Improve the UI/UX
- Add animations
- Optimize performance
- Add tests
- Improve accessibility

## 📄 License

This project is created for educational purposes. Feel free to use and modify for learning Android development.

---

**Happy Learning! 🎓**

*Built with ❤️ for Android development beginners*