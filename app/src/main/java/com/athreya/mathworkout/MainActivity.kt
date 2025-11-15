package com.athreya.mathworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.athreya.mathworkout.data.AppDatabase
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.ScoreRepositoryImpl
import com.athreya.mathworkout.data.SettingsManager
import com.athreya.mathworkout.data.ThemePreferencesManager
import com.athreya.mathworkout.navigation.Screen
import com.athreya.mathworkout.ui.screens.*
import com.athreya.mathworkout.ui.components.GlobalLeaderboardScreen
import com.athreya.mathworkout.ui.theme.AthreyasSumsTheme
import com.athreya.mathworkout.viewmodel.GameViewModel
import com.athreya.mathworkout.viewmodel.GlobalLeaderboardViewModel
import com.athreya.mathworkout.viewmodel.GlobalScoreViewModel
import com.athreya.mathworkout.viewmodel.HighScoreViewModel
import com.athreya.mathworkout.viewmodel.HomeViewModel
import com.athreya.mathworkout.viewmodel.SettingsViewModel
import com.athreya.mathworkout.viewmodel.ViewModelFactory
import com.athreya.mathworkout.viewmodel.GroupViewModel
import com.athreya.mathworkout.viewmodel.ChallengeViewModel
import kotlinx.coroutines.launch

/**
 * MainActivity - The single Activity that hosts all our Compose screens.
 * 
 * In modern Android development with Compose, we typically use a single Activity
 * and handle navigation between different screens using Compose Navigation.
 * 
 * This Activity:
 * - Sets up the app theme
 * - Initializes the navigation controller
 * - Creates ViewModels with proper dependencies
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display (modern Android UI pattern)
        enableEdgeToEdge()
        
        setContent {
            val context = LocalContext.current
            
            // Theme management
            val themePreferencesManager = remember { ThemePreferencesManager(context) }
            var currentThemeId by remember { mutableStateOf(themePreferencesManager.getCurrentThemeId()) }
            
            // Apply our app theme with dynamic theme support
            AthreyasSumsTheme(themeId = currentThemeId) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start the app navigation
                    MathWorkoutApp(
                        currentThemeId = currentThemeId,
                        onThemeChanged = { newThemeId ->
                            currentThemeId = newThemeId
                        }
                    )
                }
            }
        }
    }
}

/**
 * Main app composable that sets up navigation and dependency injection.
 * 
 * This is where we:
 * - Create singletons for database and settings manager
 * - Set up the navigation controller
 * - Create ViewModels with proper dependencies
 * - Define the navigation graph
 */
@Composable
fun MathWorkoutApp(
    currentThemeId: String,
    onThemeChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Create singleton instances of our data layer
    // In a real production app, you'd use a proper DI framework like Dagger/Hilt
    val database = AppDatabase.getDatabase(context)
    val settingsManager = SettingsManager(context)
    val achievementManager = remember { com.athreya.mathworkout.data.AchievementManager(context) }
    
    // Navigation controller manages navigation between screens
    val navController = rememberNavController()
    
    // Create ViewModels with proper dependencies
    // These will be recreated only when the composable is removed from composition
    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(settingsManager)
    }
    
    val gameViewModel: GameViewModel = viewModel {
        GameViewModel(settingsManager)
    }
    
    val highScoreViewModel: HighScoreViewModel = viewModel {
        HighScoreViewModel(database.highScoreDao(), context)
    }
    
    val globalScoreViewModel: GlobalScoreViewModel = viewModel {
        GlobalScoreViewModel(context)
    }
    
    val globalLeaderboardViewModel: GlobalLeaderboardViewModel = viewModel {
        GlobalLeaderboardViewModel(
            ScoreRepositoryImpl(context)
        )
    }
    
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(context)
    }
    
    val dailyChallengeViewModel: com.athreya.mathworkout.viewmodel.DailyChallengeViewModel = viewModel {
        com.athreya.mathworkout.viewmodel.DailyChallengeViewModel(context.applicationContext as android.app.Application)
    }
    
    val groupViewModel: GroupViewModel = viewModel {
        GroupViewModel(context.applicationContext as android.app.Application)
    }
    
    val challengeViewModel: ChallengeViewModel = viewModel {
        ChallengeViewModel(context.applicationContext as android.app.Application)
    }
    
    // Set up the navigation graph
    MathWorkoutNavigation(
        navController = navController,
        currentThemeId = currentThemeId,
        settingsViewModel = settingsViewModel,
        gameViewModel = gameViewModel,
        highScoreViewModel = highScoreViewModel,
        globalScoreViewModel = globalScoreViewModel,
        globalLeaderboardViewModel = globalLeaderboardViewModel,
        homeViewModel = homeViewModel,
        dailyChallengeViewModel = dailyChallengeViewModel,
        groupViewModel = groupViewModel,
        challengeViewModel = challengeViewModel,
        achievementManager = achievementManager,
        onThemeChanged = onThemeChanged
    )
    
    // TODO: Fix achievement notification observer - currently causing crashes
    // Box(modifier = Modifier.fillMaxSize()) {
    //     // Achievement notification observer (overlays on top)
    //     com.athreya.mathworkout.ui.components.AchievementNotificationObserver(
    //         achievementManager = achievementManager
    //     )
    // }
}

/**
 * Navigation setup for the Math Workout app.
 * 
 * This composable defines all the routes and how to navigate between them.
 * Compose Navigation uses a NavHost to manage different destinations.
 * 
 * @param navController The navigation controller that manages navigation
 * @param settingsViewModel ViewModel for settings screen
 * @param gameViewModel ViewModel for game screen
 * @param highScoreViewModel ViewModel for high score and results screens
 */
@Composable
fun MathWorkoutNavigation(
    navController: NavHostController,
    currentThemeId: String,
    settingsViewModel: SettingsViewModel,
    onThemeChanged: (String) -> Unit = {},
    gameViewModel: GameViewModel,
    highScoreViewModel: HighScoreViewModel,
    globalScoreViewModel: GlobalScoreViewModel,
    globalLeaderboardViewModel: GlobalLeaderboardViewModel,
    homeViewModel: HomeViewModel,
    dailyChallengeViewModel: com.athreya.mathworkout.viewmodel.DailyChallengeViewModel,
    groupViewModel: GroupViewModel,
    challengeViewModel: ChallengeViewModel,
    achievementManager: com.athreya.mathworkout.data.AchievementManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // Start at home screen
    ) {
        // Home Screen - The main menu
        composable(Screen.Home.route) {
            HomeScreen(
                onGameModeSelected = { gameMode ->
                    // Navigate to appropriate screen based on game mode
                    if (gameMode == GameMode.SUDOKU) {
                        navController.navigate(Screen.Sudoku.route)
                    } else {
                        navController.navigate(Screen.Game.createRoute(gameMode, isDailyChallenge = false))
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onHighScoresClick = {
                    navController.navigate(Screen.HighScores.route)
                },
                onGlobalScoreClick = {
                    navController.navigate(Screen.GlobalLeaderboard.route)
                },
                onDailyChallengeClick = {
                    navController.navigate(Screen.DailyChallenge.route)
                },
                onGroupsClick = {
                    navController.navigate(Screen.Groups.route)
                },
                gameViewModel = gameViewModel,
                homeViewModel = homeViewModel
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onThemesClick = {
                    navController.navigate(Screen.ThemeSelector.route)
                },
                onAchievementsClick = {
                    navController.navigate(Screen.Achievements.route)
                },
                achievementManager = achievementManager,
                viewModel = settingsViewModel
            )
        }
        
        // Theme Selector Screen
        composable(Screen.ThemeSelector.route) {
            ThemeSelectorScreen(
                currentThemeId = currentThemeId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onThemeSelected = { themeId ->
                    onThemeChanged(themeId)
                }
            )
        }
        
        // Achievements Screen
        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        // Game Screen - Takes a game mode parameter
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameMode") { type = NavType.StringType },
                navArgument("isDailyChallenge") { type = NavType.BoolType; defaultValue = false },
                navArgument("challengeId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Extract the game mode from navigation arguments
            val gameModeString = backStackEntry.arguments?.getString("gameMode") ?: return@composable
            val isDailyChallenge = backStackEntry.arguments?.getBoolean("isDailyChallenge") ?: false
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            val gameMode = try {
                GameMode.valueOf(gameModeString)
            } catch (e: IllegalArgumentException) {
                GameMode.ADDITION_SUBTRACTION // Fallback
            }
            
            GameScreen(
                gameMode = gameMode,
                isDailyChallenge = isDailyChallenge,
                challengeId = challengeId,
                onGameComplete = { mode, difficulty, wrongAttempts, totalTime, questionsAnswered ->
                    // Calculate points using the same logic as high scores
                    val correctAnswers = questionsAnswered - wrongAttempts
                    val points = com.athreya.mathworkout.data.HighScore.calculatePoints(
                        questionsAnswered = questionsAnswered,
                        correctAnswers = correctAnswers,
                        wrongAttempts = wrongAttempts,
                        timeTaken = totalTime,
                        difficulty = difficulty
                    )
                    
                    // Track achievement progress
                    val timeMultiplier = if (questionsAnswered > 0) {
                        val avgTimePerQuestion = totalTime.toFloat() / questionsAnswered
                        when {
                            avgTimePerQuestion <= 1000 -> 3.0f
                            avgTimePerQuestion <= 1200 -> 2.0f
                            else -> 1.0f
                        }
                    } else 1.0f
                    
                    achievementManager.trackGameCompletion(
                        score = points,
                        wrongAttempts = wrongAttempts,
                        timeMultiplier = timeMultiplier,
                        difficulty = com.athreya.mathworkout.data.Difficulty.valueOf(difficulty.uppercase()),
                        isDailyChallenge = isDailyChallenge
                    )
                    
                    // Update member stats in all groups
                    groupViewModel.updateMemberStatsAfterGame(points)
                    
                    // If this is a challenge, submit the result
                    if (challengeId != null) {
                        challengeViewModel.submitChallengeResult(
                            challengeId = challengeId,
                            score = points,
                            timeTaken = totalTime,
                            onSuccess = {
                                android.util.Log.d("MainActivity", "Challenge result submitted successfully")
                                // Track challenge win for achievements
                                achievementManager.trackChallengeWin()
                            },
                            onError = { error ->
                                android.util.Log.e("MainActivity", "Failed to submit challenge result: $error")
                            }
                        )
                    }
                    
                    // Navigate to results screen with game data
                    // Score will be saved by the ResultsScreen using the registered player name
                    navController.navigate(
                        Screen.Results.createRoute(mode, difficulty, wrongAttempts, totalTime, questionsAnswered, isDailyChallenge)
                    ) {
                        // Remove the game screen from the back stack
                        // This prevents users from going back to the finished game
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                },
                viewModel = gameViewModel
            )
        }
        
        // Results Screen - Takes multiple parameters from the completed game
        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("gameMode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("wrongAttempts") { type = NavType.IntType },
                navArgument("totalTime") { type = NavType.LongType },
                navArgument("questionsAnswered") { type = NavType.IntType },
                navArgument("isDailyChallenge") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            // Extract all the game result data from navigation arguments
            val arguments = backStackEntry.arguments ?: return@composable
            val gameModeString = arguments.getString("gameMode") ?: return@composable
            val difficulty = arguments.getString("difficulty") ?: return@composable
            val wrongAttempts = arguments.getInt("wrongAttempts")
            val totalTime = arguments.getLong("totalTime")
            val questionsAnswered = arguments.getInt("questionsAnswered")
            val isDailyChallenge = arguments.getBoolean("isDailyChallenge")
            
            val gameMode = try {
                GameMode.valueOf(gameModeString)
            } catch (e: IllegalArgumentException) {
                GameMode.ADDITION_SUBTRACTION // Fallback
            }
            
            ResultsScreen(
                gameMode = gameMode,
                difficulty = difficulty,
                wrongAttempts = wrongAttempts,
                totalTime = totalTime,
                questionsAnswered = questionsAnswered,
                isDailyChallenge = isDailyChallenge,
                onViewHighScores = {
                    navController.navigate(Screen.HighScores.route)
                },
                onHomeClick = {
                    // Navigate to home and clear the entire back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = highScoreViewModel,
                dailyChallengeViewModel = dailyChallengeViewModel
            )
        }
        
        // High Scores Screen
        composable(Screen.HighScores.route) {
            HighScoreScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                viewModel = highScoreViewModel
            )
        }
        
        // Sudoku Screen
        composable(Screen.Sudoku.route) {
            SudokuScreen(
                onBackPressed = {
                    navController.navigateUp()
                }
            )
        }
        
        // Global Score Screen
        composable(Screen.GlobalScore.route) {
            GlobalScoreScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        
        // Global Leaderboard Screen
        composable(Screen.GlobalLeaderboard.route) {
            GlobalLeaderboardScreen(
                viewModel = globalLeaderboardViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Daily Challenge Screen
        composable(Screen.DailyChallenge.route) {
            val uiState = dailyChallengeViewModel.uiState.collectAsState()
            DailyChallengeScreen(
                todaysChallenge = uiState.value.todaysChallenge,
                completedChallenges = uiState.value.completedChallenges,
                onStartChallenge = { challenge ->
                    // Navigate to game screen with daily challenge mode
                    val gameMode = when (challenge.gameMode) {
                        "Addition", "Subtraction" -> GameMode.ADDITION_SUBTRACTION
                        "Multiplication", "Division" -> GameMode.MULTIPLICATION_DIVISION
                        "TestMe" -> GameMode.TEST_ME
                        "BrainTeaser" -> GameMode.BRAIN_TEASER
                        "Sudoku" -> GameMode.SUDOKU
                        else -> GameMode.TEST_ME
                    }
                    if (gameMode == GameMode.SUDOKU) {
                        navController.navigate(Screen.Sudoku.route)
                    } else {
                        navController.navigate(Screen.Game.createRoute(gameMode, isDailyChallenge = true))
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        
        // Groups Screen
        composable(Screen.Groups.route) {
            GroupsScreen(
                groupViewModel = groupViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onGroupClick = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                }
            )
        }
        
        // Group Detail Screen
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                groupViewModel = groupViewModel,
                challengeViewModel = challengeViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onCreateChallenge = { gId ->
                    navController.navigate(Screen.Challenges.route)
                }
            )
        }
        
        // Challenges Screen
        composable(Screen.Challenges.route) {
            val coroutineScope = rememberCoroutineScope()
            val challengeContext = LocalContext.current
            val challengeSettingsManager = SettingsManager(challengeContext)
            
            ChallengesScreen(
                challengeViewModel = challengeViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onChallengeClick = { challengeId ->
                    // Get the challenge to extract game mode and difficulty
                    val challenge = challengeViewModel.getChallenge(challengeId)
                    if (challenge != null) {
                        // Temporarily update game settings to match challenge
                        coroutineScope.launch {
                            challengeSettingsManager.updateDifficulty(challenge.difficulty)
                            challengeSettingsManager.updateQuestionCount(challenge.questionCount)
                        }
                        
                        navController.navigate(
                            Screen.Game.createRoute(
                                gameMode = challenge.gameMode,
                                isDailyChallenge = false,
                                challengeId = challengeId
                            )
                        )
                    }
                }
            )
        }
    }
}