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
import com.athreya.mathworkout.data.GameType
import com.athreya.mathworkout.data.GameDifficulty
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
        
        // Request notification permission for Android 13+
        com.athreya.mathworkout.utils.NotificationHelper.requestNotificationPermission(this)
        
        // Get and log FCM token for testing
        com.athreya.mathworkout.utils.NotificationHelper.getFCMToken { token ->
            android.util.Log.d("MainActivity", "FCM Token: $token")
        }
        
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
    val userPreferences = remember { com.athreya.mathworkout.data.UserPreferencesManager(context) }
    val badgeManager = remember { com.athreya.mathworkout.data.BadgeManager(userPreferences) }
    
    // Daily login reward state
    var showDailyReward by remember { mutableStateOf(false) }
    var dailyRewardData by remember { mutableStateOf<Triple<com.athreya.mathworkout.data.DailyReward, Int, com.athreya.mathworkout.data.DailyLoginManager>?>(null) }
    val scope = rememberCoroutineScope()
    
    // Check for daily login reward on app start
    LaunchedEffect(Unit) {
        val dailyLoginManager = com.athreya.mathworkout.data.DailyLoginManager(userPreferences)
        val (currentStreak, hasNewReward) = dailyLoginManager.checkDailyLogin()
        
        if (hasNewReward) {
            val reward = com.athreya.mathworkout.data.DailyRewards.getRewardForDay(currentStreak)
            dailyRewardData = Triple(reward, currentStreak, dailyLoginManager)
            showDailyReward = true
        }
    }
    
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
    
    val avatarViewModel: com.athreya.mathworkout.viewmodel.AvatarViewModel = viewModel {
        com.athreya.mathworkout.viewmodel.AvatarViewModel(context.applicationContext as android.app.Application)
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
        avatarViewModel = avatarViewModel,
        groupViewModel = groupViewModel,
        challengeViewModel = challengeViewModel,
        achievementManager = achievementManager,
        badgeManager = badgeManager,
        userPreferencesManager = userPreferences,
        onThemeChanged = onThemeChanged
    )
    
    // Daily reward dialog
    if (showDailyReward && dailyRewardData != null) {
        val (reward, streak, loginManager) = dailyRewardData!!
        com.athreya.mathworkout.ui.dialogs.DailyRewardDialog(
            reward = reward,
            currentStreak = streak,
            onDismiss = { showDailyReward = false },
            onClaim = {
                scope.launch {
                    // Award XP
                    userPreferences.addXP(reward.xpReward)
                    
                    // Handle special rewards
                    reward.specialReward?.let { special ->
                        when (special) {
                            is com.athreya.mathworkout.data.SpecialReward.MathematicianUnlock -> {
                                // Auto-unlock the mathematician
                                database.avatarDao().unlockAvatar(special.mathematicianId, System.currentTimeMillis())
                            }
                            is com.athreya.mathworkout.data.SpecialReward.Badge -> {
                                // Badge unlocking handled by BadgeManager
                            }
                        }
                    }
                    
                    // Check for new badges from login streak
                    val newBadges = badgeManager.trackLoginStreak(streak)
                    // TODO: Show badge notification if any newBadges
                    
                    showDailyReward = false
                }
            }
        )
    }
    
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
    avatarViewModel: com.athreya.mathworkout.viewmodel.AvatarViewModel,
    groupViewModel: GroupViewModel,
    challengeViewModel: ChallengeViewModel,
    achievementManager: com.athreya.mathworkout.data.AchievementManager,
    badgeManager: com.athreya.mathworkout.data.BadgeManager,
    userPreferencesManager: com.athreya.mathworkout.data.UserPreferencesManager
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
                        navController.navigate(Screen.Sudoku.createRoute(isDailyChallenge = false))
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
                onMathematiciansClick = {
                    navController.navigate(Screen.Mathematicians.route)
                },
                onBadgesClick = {
                    navController.navigate(Screen.Badges.route)
                },
                onMathTricksClick = {
                    navController.navigate(Screen.MathTricks.route)
                },
                onInteractiveGamesClick = {
                    navController.navigate(Screen.InteractiveGames.route)
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
            
            // Create coroutine scope for async operations
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            
            GameScreen(
                gameMode = gameMode,
                isDailyChallenge = isDailyChallenge,
                challengeId = challengeId,
                onGameComplete = { mode, difficulty, wrongAttempts, totalTime, questionsAnswered ->
                    // Launch coroutine to handle async operations
                    scope.launch {
                        try {
                            // Calculate base points using the same logic as high scores
                            val correctAnswers = questionsAnswered - wrongAttempts
                            val basePoints = com.athreya.mathworkout.data.HighScore.calculatePoints(
                                questionsAnswered = questionsAnswered,
                                correctAnswers = correctAnswers,
                                wrongAttempts = wrongAttempts,
                                timeTaken = totalTime,
                                difficulty = difficulty
                            )
                            
                            // Calculate bonus multiplier from daily challenge streak
                            val bonusMultiplier = try {
                                dailyChallengeViewModel.getStreakMultiplier()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                1.0f // Default multiplier on error
                            }
                            
                            // Apply bonus to get final score (same as shown in ResultsScreen)
                            val finalScore = com.athreya.mathworkout.data.HighScore.applyBonus(basePoints, bonusMultiplier)
                            
                            // Track achievement progress with final score
                            val timeMultiplier = if (questionsAnswered > 0) {
                                val avgTimePerQuestion = totalTime.toFloat() / questionsAnswered
                                when {
                                    avgTimePerQuestion <= 1000 -> 3.0f
                                    avgTimePerQuestion <= 1200 -> 2.0f
                                    else -> 1.0f
                                }
                            } else 1.0f
                            
                            achievementManager.trackGameCompletion(
                                score = finalScore,  // Use final score with bonus applied
                                wrongAttempts = wrongAttempts,
                                timeMultiplier = timeMultiplier,
                                difficulty = com.athreya.mathworkout.data.Difficulty.valueOf(difficulty.uppercase()),
                                isDailyChallenge = isDailyChallenge
                            )
                            
                            // Update member stats in all groups (use final score with bonus)
                            groupViewModel.updateMemberStatsAfterGame(finalScore)
                            
                            // If this is a challenge, submit the result (use final score with bonus)
                            if (challengeId != null) {
                                challengeViewModel.submitChallengeResult(
                                    challengeId = challengeId,
                                    score = finalScore,
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
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error in onGameComplete", e)
                            e.printStackTrace()
                        }
                        
                        // Navigate to results screen with game data (outside try-catch to ensure navigation happens)
                        // Score will be saved by the ResultsScreen using the registered player name
                        navController.navigate(
                            Screen.Results.createRoute(mode, difficulty, wrongAttempts, totalTime, questionsAnswered, isDailyChallenge, challengeId)
                        ) {
                            // Remove the game screen from the back stack
                            // This prevents users from going back to the finished game
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
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
                navArgument("isDailyChallenge") { type = NavType.BoolType; defaultValue = false },
                navArgument("challengeId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
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
            val challengeId = arguments.getString("challengeId")
            
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
                challengeId = challengeId,
                onViewHighScores = {
                    navController.navigate(Screen.HighScores.route)
                },
                onHomeClick = {
                    // Navigate to home and clear the entire back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChallengesClick = {
                    // Navigate to challenges screen
                    navController.navigate(Screen.Challenges.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                viewModel = highScoreViewModel,
                dailyChallengeViewModel = dailyChallengeViewModel,
                achievementManager = achievementManager,
                badgeManager = badgeManager
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
        composable(
            route = Screen.Sudoku.route,
            arguments = listOf(
                navArgument("isDailyChallenge") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isDailyChallenge = backStackEntry.arguments?.getBoolean("isDailyChallenge") ?: false
            
            SudokuScreen(
                onBackPressed = {
                    navController.navigateUp()
                },
                isDailyChallenge = isDailyChallenge,
                onChallengeComplete = { timeTaken, wrongAttempts ->
                    if (isDailyChallenge) {
                        // Mark daily challenge as complete with actual time
                        dailyChallengeViewModel.completeChallenge(
                            timeTaken = timeTaken,
                            wrongAttempts = wrongAttempts
                        )
                    }
                },
                globalScoreViewModel = globalScoreViewModel
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
                        navController.navigate(Screen.Sudoku.createRoute(isDailyChallenge = true))
                    } else {
                        navController.navigate(Screen.Game.createRoute(gameMode, isDailyChallenge = true))
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        
        // Mathematicians Screen
        composable(Screen.Mathematicians.route) {
            com.athreya.mathworkout.ui.screens.MathematiciansScreen(
                viewModel = avatarViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Badges Screen
        composable(Screen.Badges.route) {
            BadgesScreen(
                badgeManager = badgeManager,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        
        // Math Tricks Screen
        composable(Screen.MathTricks.route) {
            MathTricksScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onTrickClick = { trickId ->
                    navController.navigate(Screen.TrickDetail.createRoute(trickId))
                }
            )
        }
        
        // Math Trick Detail Screen
        composable(
            route = Screen.TrickDetail.route,
            arguments = listOf(
                navArgument("trickId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val trickId = backStackEntry.arguments?.getString("trickId") ?: return@composable
            TrickDetailScreen(
                trickId = trickId,
                onBackClick = { navController.navigateUp() },
                onStartPractice = { tId ->
                    navController.navigate(Screen.TrickPractice.createRoute(tId))
                }
            )
        }
        
        // Math Trick Practice Screen
        composable(
            route = Screen.TrickPractice.route,
            arguments = listOf(
                navArgument("trickId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val trickId = backStackEntry.arguments?.getString("trickId") ?: return@composable
            TrickPracticeScreen(
                trickId = trickId,
                onBackClick = { navController.navigateUp() },
                onComplete = { score, total ->
                    // Could save progress here
                    // For now, just stay on the completion screen
                }
            )
        }
        
        // Interactive Games Screen
        composable(Screen.InteractiveGames.route) {
            InteractiveGamesScreen(
                onBackClick = { navController.navigateUp() },
                onDailyRiddleClick = {
                    navController.navigate(Screen.DailyRiddle.route)
                },
                onGameClick = { gameType, difficulty ->
                    navController.navigate(Screen.GamePlay.createRoute(gameType, difficulty))
                }
            )
        }
        
        // Daily Riddle Screen
        composable(Screen.DailyRiddle.route) {
            DailyRiddleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Game Play Screen
        composable(Screen.GamePlay.route) { backStackEntry ->
            val gameType = backStackEntry.arguments?.getString("gameType") ?: return@composable
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: return@composable
            
            GamePlayScreen(
                gameType = GameType.valueOf(gameType),
                difficulty = GameDifficulty.valueOf(difficulty),
                onBackClick = { navController.navigateUp() },
                userPreferencesManager = userPreferencesManager
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
                        // Temporarily update difficulty to match challenge
                        // NOTE: We don't update question count - use user's settings instead
                        coroutineScope.launch {
                            challengeSettingsManager.updateDifficulty(challenge.difficulty)
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