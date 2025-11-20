# Athreya's Math Workout 🧮

A comprehensive Android math training application built with **Kotlin**, **Jetpack Compose**, and modern **Android architecture**. Transform mental math practice into an exciting adventure with achievements, badges, themes, daily challenges, interactive games, and global competition!

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-1.5.0-green.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A feature-rich math workout app designed for students, professionals, and math enthusiasts. Features achievements, unlockable badges, themes, daily challenges, interactive games (Sudoku, Math Tricks, Daily Riddles), social features, and global leaderboards - all wrapped in a beautiful, customizable interface.

## 📱 Screenshots

[Add screenshots here showing: Home Screen, Game Screen, Achievements, Badges, Themes, Daily Challenges, Leaderboard, Sudoku, Math Tricks]

---

## ✨ Key Features

### 🎮 Game Modes
- **Quick Play** - Jump straight into action with customizable difficulty
- **Practice Mode** - Perfect your skills at your own pace  
- **Daily Challenges** - Fresh math problems every 24 hours with streak tracking
- **Timed Sessions** - Race against the clock for maximum scores
- **Custom Difficulty** - Easy, Medium, and Hard levels
- **Operation Types**: Addition, Subtraction, Multiplication, Division, Mixed, Brain Teasers

### � Interactive Games
- **Sudoku** - Classic 9×9 puzzle with difficulty settings
  - Integrated with global scoring system
  - Smart hint system with conflict detection
  - Timer tracking and score calculation
  - Visual conflict highlighting
- **Math Tricks Library** - Learn mental calculation shortcuts
  - Step-by-step explanations
  - Interactive practice mode
  - Progress tracking
- **Daily Riddles** - Solve a new math riddle every day
  - Brain-teasing challenges
  - Daily rewards
  - Riddle history tracking

### 🏆 Achievement & Badge System
- **50+ Unique Badges** across 5 categories:
  - ⚡ **Speed** - Complete games quickly
  - 🎯 **Accuracy** - Perfect scores
  - 📚 **Collection** - Complete all game modes
  - ⚔️ **Challenge** - Win player battles
  - � **Dedication** - Daily streak achievements
- **5 Rarity Levels**: Bronze → Silver → Gold → Platinum → Diamond
- **Badge Display**:
  - Home screen (below username)
  - Global leaderboard (next to entries)
  - Group leaderboard (your entry)
  - Challenge screens (challenger badges)
- **5-Tier Rank System**: Beginner → Amateur → Expert → Master → Grandmaster
- **Progress Tracking** with visual indicators
- **Achievement Notifications** with celebrations

### 💡 Smart Hint System
- **Per-Question Tracking** - Wrong attempts counted per question
- **Progressive Hints** - Educational hints after 2 wrong attempts
- **Contextual Guidance**:
  - Addition: Place value breakdown with examples
  - Subtraction: Inverse relationship explanations
  - Multiplication: Repeated addition or factoring
  - Division: Grouping concepts
- **Auto-Progression** - Show answer after 4 attempts, auto-advance
- **Educational Focus** - Learn from mistakes with detailed explanations

### 🎨 Beautiful Themes
- **7 Stunning Themes** including Dark Mode
- **Unlockable Themes**:
  - 🦸 **Marvel Theme** - Unlock by scoring 300+ in a single game
  - 🦇 **DC Theme** - Unlock by completing 50 games
  - 💡 **Neon Theme** - Unlock with 30+ speed games (3× multiplier)
  - 🌊 **Ocean Theme** - Unlock by maintaining a 7-day streak
  - 🌅 **Sunset Theme** - Unlock by earning 5,000 total points
- **Dynamic Color Schemes** that adapt to each theme
- **Persistent Theme Selection** across app sessions
- **Custom Theme Creator** (coming soon)

### 📊 Comprehensive Statistics
- Track **total games played** and **perfect scores**
- Monitor **current streak** and **high scores**
- View **rank progression** and **total points**
- **Detailed performance analytics**
- **Achievement & badge progress** with visual indicators
- **Historical data** for all game sessions
- **Game mode breakdown** and statistics

### 🌍 Global Competition
- **Firebase-powered global leaderboard**
- Compete with players **worldwide**
- **Real-time score updates** and rankings
- **Username registration** with availability checking
- View **top performers** with filtering
- Track your **global ranking**
- **Filter by game mode and difficulty**
- **Badge display** showing achievements

### 👥 Social Features
- **Create and join math challenge groups**
- Compete with **friends and family**
- **Group leaderboards** with rankings
- **Send and receive challenges** to other players
- **Head-to-head battles** with turn-based gameplay
- **Challenge history** tracking
- **Group member management** with roles (Admin/Member)
- **Share group codes** to invite players
- **Private competitions** within groups

### ⏰ Daily Challenges
- **New challenges every day** at midnight
- **Special rewards** for completion
- **Streak multipliers**: 3 days (1.5×), 7 days (2×), 14 days (2.5×), 30 days (3×)
- **Streak bonuses apply to ALL games**
- **Daily leaderboard** to climb
- **Challenge history** tracking with completion dates
- **Notifications** for new challenges

### 🎯 Smart Learning
- **Adaptive difficulty** based on performance
- **Educational hints** with step-by-step explanations
- **Instant feedback** on answers
- **Progressive hint system** (hint at 2 attempts, answer at 4)
- **Learn from mistakes** with detailed breakdowns
- **Auto-progression** to prevent getting stuck
- **Context-specific guidance** for each operation type

### ⚡ Game Features
- **Time multipliers** for bonus points (1.5×, 2×, 3×)
- **Streak multipliers** from daily challenges
- **Clean, intuitive interface** with Material Design 3
- **Smooth animations** and celebrations
- **Offline mode** available for local play
- **Auto-submission** when expected digits are entered
- **Visual feedback** for correct/wrong answers
- **Comprehensive in-app guide** in Settings

### 📈 Progress Tracking
- **Detailed game history** with timestamps
- **High score records** per difficulty level
- **Personal best tracking** across all modes
- **Challenge completion stats**
- **Badge progress** with unlock requirements
- **Achievement tracking** with visual progress bars
- **Rank advancement** notifications

### 🎁 Rewards & Unlockables
- Unlock **themes through achievements**
- Earn **badges** across 5 categories
- **Rank up system** with 5 tiers
- **Special rewards** for milestones
- **50+ collectible badges** with rarities
- **Visual badges** displayed throughout app

---

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Kotlin 1.9.0
- **UI Framework**: Jetpack Compose (100% declarative UI)
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room (SQLite abstraction)
- **Settings**: SharedPreferences & DataStore
- **Backend**: Firebase (Firestore, Authentication)
- **Navigation**: Compose Navigation
- **State Management**: StateFlow, MutableState
- **Dependency Injection**: Manual DI (ViewModelFactory)
- **Coroutines**: Kotlin Coroutines for async operations
- **Material Design**: Material 3 components

### Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│  (Compose Screens, ViewModels, Navigation)              │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│  (Use Cases, Business Logic, Data Models)                │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Local DB   │  │   Firebase   │  │ Preferences  │  │
│  │   (Room)     │  │  (Firestore) │  │  (DataStore) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Project Structure
```
app/src/main/java/com/athreya/mathworkout/
├── data/                               # Data layer
│   ├── AppDatabase.kt                  # Room database configuration
│   ├── GameSettings.kt                 # Game settings data models
│   ├── HighScore.kt                    # High score entity
│   ├── HighScoreDao.kt                 # Local database queries
│   ├── Achievement.kt                  # Achievement data models
│   ├── AchievementManager.kt           # Achievement logic & tracking
│   ├── Badge.kt                        # Badge system (50+ badges)
│   ├── BadgeManager.kt                 # Badge unlock & progress tracking
│   ├── Rank.kt                         # Rank system definitions
│   ├── ThemePreferencesManager.kt      # Theme selection persistence
│   ├── UserPreferencesManager.kt       # User settings management
│   ├── DailyChallenge.kt              # Daily challenge models
│   ├── DailyStreak.kt                 # Streak tracking & multipliers
│   ├── FirebaseScoreService.kt        # Firebase integration
│   ├── Group.kt                       # Group & challenge models
│   ├── Mathematician.kt               # Famous mathematicians library
│   ├── MathTrick.kt                   # Math tricks data models
│   ├── SudokuEngine.kt                # Sudoku generation & validation
│   ├── ScoreRepository.kt             # Score data abstraction
│   ├── network/                       # Network layer
│   │   ├── GlobalScoreApiService.kt   # API definitions
│   │   └── GlobalScoreModels.kt       # Network models
│   ├── repository/                    # Repository implementations
│   │   ├── ChallengeRepository.kt
│   │   ├── GroupRepository.kt
│   │   └── GlobalScoreRepository.kt
│   └── social/                        # Social features
│       ├── Group.kt                   # Group models
│       ├── GroupDao.kt                # Group database queries
│       ├── GroupFirebaseService.kt    # Firebase group sync
│       ├── Challenge.kt               # Challenge models
│       ├── ChallengeDao.kt            # Challenge queries
│       └── SocialSyncManager.kt       # Social data sync
│
├── ui/                                # UI layer
│   ├── screens/                       # Screen composables
│   │   ├── HomeScreen.kt             # Main menu with badges
│   │   ├── GameScreen.kt             # Math problem gameplay
│   │   ├── ResultsScreen.kt          # Game results with confetti
│   │   ├── SettingsScreen.kt         # Settings & How to Play guide
│   │   ├── HighScoreScreen.kt        # Local high scores
│   │   ├── AchievementsScreen.kt     # Achievement tracking UI
│   │   ├── BadgesScreen.kt           # Badge collection display
│   │   ├── ThemeSelectorScreen.kt    # Theme customization
│   │   ├── DailyChallengeScreen.kt   # Daily challenges & streaks
│   │   ├── GlobalScoreScreen.kt      # Global leaderboard
│   │   ├── GroupsScreen.kt           # Social groups
│   │   ├── GroupDetailScreen.kt      # Group details & leaderboard
│   │   ├── ChallengesScreen.kt       # Player challenges
│   │   ├── MathematiciansScreen.kt   # Famous mathematicians
│   │   ├── MathTricksScreen.kt       # Math tricks library
│   │   ├── TrickDetailScreen.kt      # Trick explanations
│   │   ├── TrickPracticeScreen.kt    # Interactive practice
│   │   ├── InteractiveGamesScreen.kt # Games hub
│   │   ├── SudokuScreen.kt           # Sudoku gameplay
│   │   ├── DailyRiddleScreen.kt      # Daily math riddles
│   │   └── GamePlayScreen.kt         # Generic game play
│   │
│   ├── components/                    # Reusable UI components
│   │   ├── Animations.kt             # Animation utilities
│   │   ├── ConfettiAnimation.kt      # Confetti particle system
│   │   ├── AchievementNotifications.kt # Achievement popups
│   │   ├── BadgeDisplay.kt           # Badge UI components
│   │   ├── GlobalLeaderboardScreen.kt # Leaderboard with badges
│   │   ├── AnimatedCounter.kt        # Animated number displays
│   │   ├── ProgressIndicators.kt     # Custom progress bars
│   │   ├── PlayerNameDialog.kt       # User input dialogs
│   │   ├── UserRegistrationDialog.kt # User registration
│   │   └── CreateChallengeDialog.kt  # Challenge creation
│   │
│   └── theme/                        # Theming system
│       ├── AppTheme.kt               # 7 theme definitions
│       ├── Theme.kt                  # Theme configuration
│       ├── Color.kt                  # Color palettes
│       └── Type.kt                   # Typography system
│
├── viewmodel/                        # ViewModels
│   ├── GameViewModel.kt              # Game state management
│   ├── SettingsViewModel.kt          # Settings state
│   ├── HighScoreViewModel.kt         # High score logic
│   ├── HomeViewModel.kt              # Home screen state
│   ├── DailyChallengeViewModel.kt    # Daily challenge logic
│   ├── GlobalScoreViewModel.kt       # Global score state
│   ├── GlobalLeaderboardViewModel.kt # Leaderboard logic
│   ├── GroupViewModel.kt             # Group management
│   ├── ChallengeViewModel.kt         # Challenge state
│   └── ViewModelFactory.kt           # ViewModel creation
│
├── navigation/                       # Navigation
│   └── Screen.kt                     # Navigation routes & setup
│
└── MainActivity.kt                   # App entry point & navigation graph
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK 34 (targetSdk)
- Minimum Android 7.0 (API 24)
- Firebase project (for online features)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/hari87gxs/1standroidgame.git
   cd 1standroidgame
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **Firebase Setup** (Required for online features)
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app to your Firebase project
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Firestore Database and Authentication

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button (▶️)

### Firebase Configuration

1. **Firestore Rules** (Deploy using `deploy_firestore_index.sh`)
   ```javascript
   // See firestore.rules for complete configuration
   ```

2. **Firestore Indexes** (Auto-deployed)
   ```json
   // See firestore.indexes.json
   ```

3. **Authentication**
   - Enable Anonymous Authentication in Firebase Console
   - No additional configuration needed

---

## 📦 Dependencies

### Core Android
```gradle
// Jetpack Compose
implementation "androidx.compose.ui:ui:1.5.4"
implementation "androidx.compose.material3:material3:1.1.2"
implementation "androidx.compose.ui:ui-tooling-preview:1.5.4"
implementation "androidx.activity:activity-compose:1.8.1"
implementation "androidx.navigation:navigation-compose:2.7.5"

// Room Database
implementation "androidx.room:room-runtime:2.6.0"
implementation "androidx.room:room-ktx:2.6.0"
kapt "androidx.room:room-compiler:2.6.0"

// DataStore
implementation "androidx.datastore:datastore-preferences:1.0.0"

// Lifecycle & ViewModel
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2"
implementation "androidx.lifecycle:lifecycle-runtime-compose:2.6.2"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
```

### Firebase
```gradle
// Firebase Platform
implementation platform("com.google.firebase:firebase-bom:32.5.0")
implementation "com.google.firebase:firebase-firestore-ktx"
implementation "com.google.firebase:firebase-auth-ktx"
implementation "com.google.firebase:firebase-analytics-ktx"
```

### Other Libraries
```gradle
// Gson for JSON parsing
implementation "com.google.code.gson:gson:2.10.1"

// Material Icons Extended
implementation "androidx.compose.material:material-icons-extended:1.5.4"
```

---

## 🎮 How to Play

### Quick Start
1. **Launch the app** and see your current rank badge on the home screen
2. **Select a game mode**: Quick Play, Practice, or Daily Challenge
3. **Choose difficulty**: Easy, Medium, Hard, or Expert
4. **Solve math problems** as quickly and accurately as possible
5. **Earn points** based on speed and accuracy
6. **Unlock achievements** and new themes as you progress!

### Earning Points
- **Base Points**: Correct answer = 10 points
- **Time Multipliers**:
  - ⚡ 1.5× multiplier: Answer within 5 seconds
  - ⚡⚡ 2× multiplier: Answer within 3 seconds  
  - ⚡⚡⚡ 3× multiplier: Answer within 2 seconds
- **Penalties**: Wrong answer = -5 points
- **Bonus**: Perfect game (no errors) = 50 bonus points

### Unlocking Themes
- **Marvel**: Score 300+ in a single game
- **DC**: Complete 50 total games
- **Neon**: Complete 30+ games with 3× time multiplier
- **Ocean**: Maintain a 7-day login streak
- **Sunset**: Accumulate 5,000 total points

### Ranking Up
- **Beginner** (🌱): 0 - 999 points
- **Amateur** (📚): 1,000 - 4,999 points
- **Expert** (🎓): 5,000 - 14,999 points
- **Master** (⚡): 15,000 - 49,999 points
- **Grandmaster** (👑): 50,000+ points

---

## 🔧 Configuration

### App Settings
Access via Settings screen:
- **Difficulty**: Easy (1-10), Medium (1-100), Hard (1-1000), Expert (1-10000)
- **Question Count**: 10, 20, or 50 questions per game
- **Operations**: Addition, Subtraction, Multiplication, Division, Mixed
- **Theme**: Choose from unlocked themes
- **Achievements**: View progress and unlock status

### Reset Achievements (Testing)
- Go to Settings → Scroll down
- Tap "🔄 Reset All Achievements (Testing)"
- Confirm reset
- All achievements and statistics will be cleared

---

## 🧪 Testing

### Manual Testing
1. **Achievement Testing**:
   - Reset achievements in Settings
   - Play games to trigger unlocks
   - Verify notifications appear (currently disabled)
   - Check Achievements screen for progress

2. **Theme Testing**:
   - Complete achievement requirements
   - Verify themes unlock automatically
   - Test theme switching in Settings
   - Ensure theme persists across app restarts

3. **Daily Challenge Testing**:
   - Complete a daily challenge
   - Verify streak increments
   - Check leaderboard updates
   - Test challenge expiration (24 hours)

### Unit Testing (Coming Soon)
- ViewModel tests
- Repository tests  
- Use case tests
- Database migration tests

---

## 🛠️ Development

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Use dependency injection

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "Add: descriptive commit message"

# Push to GitHub
git push origin feature/your-feature-name

# Create Pull Request on GitHub
```

### Building Release APK
```bash
# Generate signed release build
./gradlew assembleRelease

# APK location
app/release/app-release.apk
```

### Keystore Configuration
See `KEYSTORE_CREATION_STEPS.md` for signing key setup.

---

## 📚 Documentation

- **[Architecture Documentation](ARCHITECTURE.md)** - Detailed technical architecture
- **[Play Store Guide](PLAYSTORE_UPDATE_GUIDE.md)** - Publishing instructions
- **[Closed Testing Guide](PLAYSTORE_CLOSED_TESTING_UPDATE.md)** - Testing phase updates
- **[Firebase Setup](FIRESTORE_INDEX_GUIDE.md)** - Firebase configuration
- **[Feature Documentation](NEW_FEATURES_IMPLEMENTATION.md)** - Implementation details

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Contribution Ideas
- [ ] Add more achievement types
- [ ] Create new theme color schemes
- [ ] Implement sound effects and music
- [ ] Add accessibility features
- [ ] Improve animations
- [ ] Add more game modes (fractions, percentages, etc.)
- [ ] Create widget for quick challenges
- [ ] Add Apple Watch support
- [ ] Implement voice control

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Harikrishnan Raguraman**
- GitHub: [@hari87gxs](https://github.com/hari87gxs)
- Repository: [1standroidgame](https://github.com/hari87gxs/1standroidgame)

---

## 🙏 Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Icons](https://fonts.google.com/icons)
- Backend powered by [Firebase](https://firebase.google.com)
- Inspired by mental math training techniques
- Thanks to the Android developer community

---

## 📊 Project Status

**Current Version**: 2.0.0 (Major Update)  
**Status**: Active Development  
**Play Store**: Closed Testing  

### Recent Updates (November 2024)
✅ Achievement system with ranks and badges  
✅ 7 beautiful themes (5 unlockable)  
✅ Enhanced statistics dashboard  
✅ Daily challenge system  
✅ Social features (groups & challenges)  
✅ Global Firebase leaderboard  
✅ Improved user interface  
✅ Performance optimizations  

### Upcoming Features
🔜 Achievement unlock animations (bug fix)  
🔜 Sound effects and music  
🔜 More game modes (fractions, algebra)  
🔜 Widget support  
🔜 Tablet optimization  
🔜 Wear OS support  

---

## 📞 Support

Having issues? Check these resources:

1. **[Documentation](ARCHITECTURE.md)** - Comprehensive technical guide
2. **[GitHub Issues](https://github.com/hari87gxs/1standroidgame/issues)** - Report bugs
3. **[Discussions](https://github.com/hari87gxs/1standroidgame/discussions)** - Ask questions

---

## ⭐ Show Your Support

Give a ⭐️ if this project helped you learn Android development!

---

**Made with ❤️ and Kotlin**

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