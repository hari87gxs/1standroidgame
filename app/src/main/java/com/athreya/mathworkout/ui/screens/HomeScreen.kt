package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.ui.components.UserRegistrationDialog
import com.athreya.mathworkout.viewmodel.GameViewModel
import com.athreya.mathworkout.viewmodel.HomeViewModel

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
 * @param onGlobalScoreClick Callback function called when user clicks global scores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameModeSelected: (GameMode) -> Unit,
    onSettingsClick: () -> Unit,
    onHighScoresClick: () -> Unit,
    onGlobalScoreClick: () -> Unit,
    onDailyChallengeClick: () -> Unit = {},
    onGroupsClick: () -> Unit = {},
    onMathematiciansClick: () -> Unit = {},
    onBadgesClick: () -> Unit = {},
    onMathTricksClick: () -> Unit = {},
    onInteractiveGamesClick: () -> Unit = {},
    gameViewModel: GameViewModel? = null,
    homeViewModel: HomeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val homeUiState by (homeViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(com.athreya.mathworkout.viewmodel.HomeUiState()) })
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
                    // Badges button
                    IconButton(onClick = onBadgesClick) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Badges"
                        )
                    }
                    // Mathematicians button
                    IconButton(onClick = onMathematiciansClick) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Mathematicians"
                        )
                    }
                    // Settings button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            // Center content horizontally
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top spacing
            Spacer(modifier = Modifier.height(24.dp))
            
            // App title
            Text(
                text = "Math Workout",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Subtitle
            Text(
                text = "Choose your challenge!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Welcome/Registration Panel - Always visible at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val playerName = homeUiState.playerName
                if (homeUiState.isUserRegistered && playerName != null) {
                    // Show welcome message with rank badge for registered users
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val achievementManager = remember { com.athreya.mathworkout.data.AchievementManager(context) }
                    val currentRank = remember { achievementManager.getCurrentRank() }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "👋 ",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = playerName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentRank.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        // Display unlocked badges
                        if (homeUiState.unlockedBadges.isNotEmpty()) {
                            com.athreya.mathworkout.ui.components.BadgeRow(
                                badges = homeUiState.unlockedBadges,
                                maxBadges = 5
                            )
                        }
                    }
                } else {
                    // Show clickable registration prompt
                    Text(
                        text = "🏆 Join the Global Challenge - Register Now!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { 
                            homeViewModel?.showRegistrationDialog() 
                        }
                    )
                }
            }
            
            // Quick Access Icons - Groups, Scores, Global
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Groups Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    onClick = onGroupsClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Groups",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                // High Scores Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    onClick = onHighScoresClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scores",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                // Global Leaderboard Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    onClick = onGlobalScoreClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Global",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily Challenge Card (Featured)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDailyChallengeClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Daily Challenge",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bonus rewards today!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go to Daily Challenge",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
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
            
            // Fun Learning Section
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "🎓 Have Fun Learning Maths!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = "Discover tricks, play games, and become a math wizard!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Learning cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Math Tricks Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    onClick = onMathTricksClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Math Tricks",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Learn shortcuts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Interactive Games Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    onClick = onInteractiveGamesClick,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎮",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Math Games",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Puzzles & riddles",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Registration Dialog
    if (homeUiState.showRegistrationDialog) {
        UserRegistrationDialog(
            onRegister = { username ->
                homeViewModel?.registerUser(username)
            },
            onDismiss = {
                homeViewModel?.hideRegistrationDialog()
            },
            onCheckUsername = { username ->
                homeViewModel?.checkUsernameAvailability(username)
            },
            isCheckingUsername = homeUiState.isCheckingUsername,
            usernameAvailable = homeUiState.usernameAvailable
        )
    }
    
    // Show registration error if any
    homeUiState.registrationError?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar or toast here
            // For now, we'll just clear the error after showing
            kotlinx.coroutines.delay(3000)
            homeViewModel?.clearRegistrationError()
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
            onHighScoresClick = { },
            onGlobalScoreClick = { }
        )
    }
}