package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.athreya.mathworkout.data.Badge
import com.athreya.mathworkout.data.BadgeCategory
import com.athreya.mathworkout.data.BadgeManager
import com.athreya.mathworkout.ui.components.BadgeCard
import com.athreya.mathworkout.ui.components.BadgeDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    badgeManager: BadgeManager,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    val badges = remember { badgeManager.getAllBadges() }
    val unlockedCount = badges.count { it.isUnlocked }
    
    // Group badges by category
    val badgesByCategory = badges.groupBy { it.category }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Badges")
                        Text(
                            text = "$unlockedCount / ${badges.size} Unlocked",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress summary
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Collection Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = unlockedCount.toFloat() / badges.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${(unlockedCount.toFloat() / badges.size * 100).toInt()}% Complete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Badges by category
            badgesByCategory.forEach { (category, categoryBadges) ->
                item {
                    Text(
                        text = getCategoryName(category),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Display badges in rows of 3
                items(categoryBadges.chunked(3)) { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeCard(
                                badge = badge,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedBadge = badge }
                            )
                        }
                        // Fill remaining spaces in incomplete rows
                        repeat(3 - rowBadges.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        // Badge detail dialog
        selectedBadge?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = { selectedBadge = null }
            )
        }
    }
}

private fun getCategoryName(category: BadgeCategory): String {
    return when (category) {
        BadgeCategory.SPEED -> "⚡ Speed"
        BadgeCategory.ACCURACY -> "🎯 Accuracy"
        BadgeCategory.COLLECTION -> "📚 Collection"
        BadgeCategory.CHALLENGE -> "⚔️ Challenges"
        BadgeCategory.DEDICATION -> "🔥 Dedication"
    }
}
