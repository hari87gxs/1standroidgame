package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.MathTrick
import com.athreya.mathworkout.data.MathTricks
import com.athreya.mathworkout.data.TrickDifficulty
import com.athreya.mathworkout.data.TrickProgressManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathTricksScreen(
    onBackClick: () -> Unit,
    onTrickClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progressManager = remember { TrickProgressManager(context) }
    val progressState by progressManager.progressState.collectAsState()
    val tricks = remember { MathTricks.getAllTricks() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Math Tricks")
                        Text(
                            text = "Learn shortcuts to solve faster!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "🎓 Welcome to Math Tricks!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Master these shortcuts to become a math wizard. Each trick includes:\n• Step-by-step explanation\n• Examples\n• Practice problems\n• Completion badge",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        // Progress indicator
                        val completedCount = progressManager.getCompletedTricksCount()
                        val totalCount = tricks.size
                        val overallProgress = progressManager.getOverallProgress()
                        
                        if (completedCount > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Progress",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$completedCount / $totalCount completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LinearProgressIndicator(
                                progress = { overallProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                        }
                    }
                }
            }
            
            // Group tricks by difficulty
            item {
                Text(
                    text = "🌟 Beginner Tricks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            items(tricks.filter { it.difficulty == TrickDifficulty.BEGINNER }) { trick ->
                val isUnlocked = progressManager.isTrickUnlocked(trick.id)
                TrickCard(
                    trick = trick,
                    progress = progressState[trick.id],
                    isLocked = !isUnlocked,
                    onClick = { if (isUnlocked) onTrickClick(trick.id) }
                )
            }
            
            item {
                Text(
                    text = "⭐ Intermediate Tricks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            items(tricks.filter { it.difficulty == TrickDifficulty.INTERMEDIATE }) { trick ->
                val isUnlocked = progressManager.isTrickUnlocked(trick.id)
                TrickCard(
                    trick = trick,
                    progress = progressState[trick.id],
                    isLocked = !isUnlocked,
                    onClick = { if (isUnlocked) onTrickClick(trick.id) }
                )
            }
            
            item {
                Text(
                    text = "🏆 Advanced Tricks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            items(tricks.filter { it.difficulty == TrickDifficulty.ADVANCED }) { trick ->
                val isUnlocked = progressManager.isTrickUnlocked(trick.id)
                TrickCard(
                    trick = trick,
                    progress = progressState[trick.id],
                    isLocked = !isUnlocked,
                    onClick = { if (isUnlocked) onTrickClick(trick.id) }
                )
            }
        }
    }
}

@Composable
fun TrickCard(
    trick: MathTrick,
    progress: com.athreya.mathworkout.data.TrickProgress?,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = progress?.isCompleted ?: false
    val bestScore = progress?.bestScore ?: 0
    val totalQuestions = progress?.totalQuestions ?: 10
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isCompleted -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isLocked -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                            isCompleted -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = trick.emoji,
                        fontSize = 28.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Trick info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trick.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        color = if (isLocked) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (isCompleted && !isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isLocked) "Complete previous trick to unlock" else trick.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (isLocked) 0.5f else 0.7f
                    ),
                    fontStyle = if (isLocked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                )
                
                if (!isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Category and difficulty tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DifficultyChip(trick.difficulty)
                        CategoryChip(trick.category.name)
                        
                        // Best score indicator
                        if (bestScore > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$bestScore/$totalQuestions",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyChip(difficulty: TrickDifficulty) {
    val (color, text) = when (difficulty) {
        TrickDifficulty.BEGINNER -> Color(0xFF4CAF50) to "Beginner"
        TrickDifficulty.INTERMEDIATE -> Color(0xFFFFC107) to "Intermediate"
        TrickDifficulty.ADVANCED -> Color(0xFFF44336) to "Advanced"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CategoryChip(category: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = category.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}
