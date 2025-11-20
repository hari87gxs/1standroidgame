package com.athreya.mathworkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.Badge
import com.athreya.mathworkout.data.BadgeCategory
import com.athreya.mathworkout.data.BadgeRarity

@Composable
fun BadgeCard(
    badge: Badge,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .aspectRatio(0.75f)
            .heightIn(min = 160.dp),
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                Color(badge.rarity.color).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge emoji/icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            if (badge.isUnlocked) {
                                Color(badge.rarity.color).copy(alpha = 0.2f)
                            } else {
                                Color.Gray.copy(alpha = 0.1f)
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (badge.isUnlocked) {
                                Color(badge.rarity.color)
                            } else {
                                Color.Gray.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.emoji,
                        fontSize = 26.sp,
                        modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.3f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Badge name
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    color = if (badge.isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                
                // Progress bar (if not unlocked)
                if (!badge.isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = badge.progressPercent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(badge.rarity.color),
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = "${badge.progress}/${badge.requirement}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // Rarity indicator
                    Text(
                        text = badge.rarity.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(badge.rarity.color)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeGrid(
    badges: List<Badge>,
    modifier: Modifier = Modifier,
    onBadgeClick: ((Badge) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(badges) { badge ->
            BadgeCard(
                badge = badge,
                onClick = onBadgeClick?.let { { it(badge) } }
            )
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(badge.rarity.color).copy(alpha = 0.2f))
                    .border(3.dp, Color(badge.rarity.color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.emoji,
                    fontSize = 40.sp
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = badge.name,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badge.rarity.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(badge.rarity.color),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!badge.isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = badge.progressPercent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(badge.rarity.color)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${badge.progress} / ${badge.requirement}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Unlocked!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(badge.rarity.color)
                        )
                    }
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

/**
 * Small badge indicator for showing on profile/leaderboards
 */
@Composable
fun BadgeIndicator(
    badge: Badge,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(badge.rarity.color).copy(alpha = 0.2f))
            .border(1.dp, Color(badge.rarity.color), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badge.emoji,
            fontSize = (size * 0.6).sp
        )
    }
}

/**
 * Row of badge indicators (for showing top badges)
 */
@Composable
fun BadgeRow(
    badges: List<Badge>,
    modifier: Modifier = Modifier,
    maxBadges: Int = 5
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        badges.take(maxBadges).forEach { badge ->
            BadgeIndicator(badge = badge)
        }
        
        if (badges.size > maxBadges) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${badges.size - maxBadges}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun Modifier.alpha(alpha: Float): Modifier {
    return this.then(
        Modifier.background(Color.White.copy(alpha = 1f - alpha))
    )
}
