package com.athreya.mathworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import com.athreya.mathworkout.data.SettingsManager
import com.athreya.mathworkout.navigation.Screen
import com.athreya.mathworkout.ui.screens.*
import com.athreya.mathworkout.ui.theme.AthreyasSumsTheme
import com.athreya.mathworkout.viewmodel.GameViewModel
import com.athreya.mathworkout.viewmodel.HighScoreViewModel
import com.athreya.mathworkout.viewmodel.SettingsViewModel

/**
 * MainActivity - The single Activity that hosts all our Compose screens.
 * 
 * In modern Android development with Compose, we typically use a single Activity
 * and handle navigation between different screens using Compose Navigation.
 * 
 * This Activity:
 * - Sets up the app theme
 * - Initializes the navigation controller
 * - Provides dependency injection (database, settings manager)
 * - Creates ViewModels with proper dependencies
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display (modern Android UI pattern)
        enableEdgeToEdge()
        
        setContent {
            // Apply our app theme
            AthreyasSumsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start the app navigation
                    MathWorkoutApp()
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
fun MathWorkoutApp() {
    val context = LocalContext.current
    
    // Create singleton instances of our data layer
    // In a real production app, you'd use a proper DI framework like Dagger/Hilt
    val database = AppDatabase.getDatabase(context)
    val settingsManager = SettingsManager(context)
    
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
        HighScoreViewModel(database.highScoreDao())
    }
    
    // Set up the navigation graph
    MathWorkoutNavigation(
        navController = navController,
        settingsViewModel = settingsViewModel,
        gameViewModel = gameViewModel,
        highScoreViewModel = highScoreViewModel
    )
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
    settingsViewModel: SettingsViewModel,
    gameViewModel: GameViewModel,
    highScoreViewModel: HighScoreViewModel
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
                        navController.navigate(Screen.Game.createRoute(gameMode))
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onHighScoresClick = {
                    navController.navigate(Screen.HighScores.route)
                },
                gameViewModel = gameViewModel
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                viewModel = settingsViewModel
            )
        }
        
        // Game Screen - Takes a game mode parameter
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract the game mode from navigation arguments
            val gameModeString = backStackEntry.arguments?.getString("gameMode") ?: return@composable
            val gameMode = try {
                GameMode.valueOf(gameModeString)
            } catch (e: IllegalArgumentException) {
                GameMode.ADDITION_SUBTRACTION // Fallback
            }
            
            GameScreen(
                gameMode = gameMode,
                onGameComplete = { mode, difficulty, wrongAttempts, totalTime ->
                    // Navigate to results screen with game data
                    navController.navigate(
                        Screen.Results.createRoute(mode, difficulty, wrongAttempts, totalTime)
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
                navArgument("totalTime") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            // Extract all the game result data from navigation arguments
            val arguments = backStackEntry.arguments ?: return@composable
            val gameModeString = arguments.getString("gameMode") ?: return@composable
            val difficulty = arguments.getString("difficulty") ?: return@composable
            val wrongAttempts = arguments.getInt("wrongAttempts")
            val totalTime = arguments.getLong("totalTime")
            
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
                onViewHighScores = {
                    navController.navigate(Screen.HighScores.route)
                },
                onHomeClick = {
                    // Navigate to home and clear the entire back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = highScoreViewModel
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
    }
}