package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.viewmodel.GameViewModel

/**
 * HomeScreen Composable - The main screen of the app.
 * 
 * This is a @Composable function, which is how we build UI in Jetpack Compose.
 * Compose is declarative - we describe what the UI should look like
 * based on the current state, and Compose handles updating the UI when state changes.
 * 
 * @param onGameModeSelected Callback function called when user selects a game mode
 * @param onSettingsClick Callback function called when user clicks settings
 * @param onHighScoresClick Callback function called when user clicks high scores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameModeSelected: (GameMode) -> Unit,
    onSettingsClick: () -> Unit,
    onHighScoresClick: () -> Unit,
    gameViewModel: GameViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Clear any previous game state when home screen loads
    LaunchedEffect(Unit) {
        gameViewModel?.clearGameState()
    }
    
    // Scaffold provides the basic structure for a Material Design screen
    Scaffold(
        topBar = {
            // TopAppBar provides the app bar at the top of the screen
            TopAppBar(
                title = { 
                    Text(
                        text = "Athreya's Sums",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                // Add action buttons to the app bar
                actions = {
                    // Settings button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    // High scores button  
                    IconButton(onClick = onHighScoresClick) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "High Scores"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Column arranges children vertically
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            // Center content both horizontally and vertically
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App title
            Text(
                text = "Math Workout",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Subtitle
            Text(
                text = "Choose your challenge!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Game mode buttons arranged vertically with spacing
            Spacer(modifier = Modifier.height(16.dp))
            
            // Addition & Subtraction button
            GameModeButton(
                text = "Addition & Subtraction",
                onClick = { onGameModeSelected(GameMode.ADDITION_SUBTRACTION) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Multiplication & Division button  
            GameModeButton(
                text = "Multiplication & Division",
                onClick = { onGameModeSelected(GameMode.MULTIPLICATION_DIVISION) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Test Me button
            GameModeButton(
                text = "Test Me",
                onClick = { onGameModeSelected(GameMode.TEST_ME) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Brain Teaser button
            GameModeButton(
                text = "Brain Teaser",
                onClick = { onGameModeSelected(GameMode.BRAIN_TEASER) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sudoku button
            GameModeButton(
                text = "🧩 Sudoku",
                onClick = { onGameModeSelected(GameMode.SUDOKU) }
            )
        }
    }
}

/**
 * Reusable composable for game mode buttons.
 * 
 * This demonstrates composition in Compose - we create smaller, reusable
 * components that we can use throughout our app.
 * 
 * @param text The text to display on the button
 * @param onClick Callback function called when button is clicked
 * @param modifier Optional modifier for customizing appearance
 */
@Composable
private fun GameModeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f) // Button takes 80% of available width
            .height(56.dp),     // Fixed height for consistency
        shape = RoundedCornerShape(12.dp), // Rounded corners
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Preview function for development.
 * 
 * @Preview annotations allow us to see our Composables in the IDE
 * without running the full app. This is great for rapid development.
 */
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onGameModeSelected = { },
            onSettingsClick = { },
            onHighScoresClick = { }
        )
    }
}