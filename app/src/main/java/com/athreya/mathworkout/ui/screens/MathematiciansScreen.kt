package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.athreya.mathworkout.data.avatar.Avatar
import com.athreya.mathworkout.data.avatar.AvatarRarity
import com.athreya.mathworkout.viewmodel.AvatarUiState
import com.athreya.mathworkout.viewmodel.AvatarViewModel

/**
 * Screen showing famous mathematicians that can be unlocked with XP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathematiciansScreen(
    viewModel: AvatarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val avatars by viewModel.avatars.collectAsState()
    val currentXP by viewModel.currentXP.collectAsState()
    val selectedAvatarId by viewModel.selectedAvatarId.collectAsState()
    val newlyUnlockedAvatar by viewModel.newlyUnlockedAvatar.collectAsState()
    
    var showUnlockDialog by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf<Avatar?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Refresh XP when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshXP()
    }
    
    // Show trivia dialog when avatar is unlocked
    LaunchedEffect(newlyUnlockedAvatar) {
        if (newlyUnlockedAvatar != null) {
            selectedAvatar = newlyUnlockedAvatar
            showUnlockDialog = true
        }
    }
    
    val (unlockedCount, totalCount) = viewModel.getUnlockProgress()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Famous Mathematicians") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // XP Display
                    Surface(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentXP XP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collection Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$unlockedCount / $totalCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
            
            // Error message
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }
            }
            
            when (uiState) {
                is AvatarUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AvatarUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as AvatarUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is AvatarUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(avatars) { avatar ->
                            MathematicianCard(
                                avatar = avatar,
                                currentXP = currentXP,
                                isSelected = avatar.avatarId == selectedAvatarId,
                                onClick = {
                                    selectedAvatar = avatar
                                    showUnlockDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog for avatar details/unlock/trivia
    if (showUnlockDialog && selectedAvatar != null) {
        MathematicianDialog(
            avatar = selectedAvatar!!,
            currentXP = currentXP,
            isSelected = selectedAvatar!!.avatarId == selectedAvatarId,
            onDismiss = {
                showUnlockDialog = false
                viewModel.clearNewlyUnlockedAvatar()
            },
            onUnlock = {
                viewModel.unlockAvatar(
                    avatarId = selectedAvatar!!.avatarId,
                    onSuccess = { 
                        // Dialog stays open to show trivia
                    },
                    onError = { error ->
                        errorMessage = error
                        showUnlockDialog = false
                    }
                )
            },
            onSelect = {
                viewModel.selectAvatar(
                    avatarId = selectedAvatar!!.avatarId,
                    onSuccess = {
                        showUnlockDialog = false
                    },
                    onError = { error ->
                        errorMessage = error
                    }
                )
            }
        )
    }
}

@Composable
fun MathematicianCard(
    avatar: Avatar,
    currentXP: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rarityColor = Color(avatar.rarity.color)
    val canAfford = !avatar.isUnlocked && currentXP >= avatar.xpCost
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (avatar.isUnlocked) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rarity badge
                Surface(
                    color = rarityColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = avatar.rarity.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Avatar portrait - Use emoji/icon representation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(rarityColor.copy(alpha = 0.2f))
                        .border(3.dp, rarityColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatar.isUnlocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Show emoji
                        Text(
                            text = avatar.emoji,
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 48.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Name
                Text(
                    text = avatar.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                // Era
                Text(
                    text = avatar.era,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // XP Cost or Status
                if (!avatar.isUnlocked) {
                    Surface(
                        color = if (canAfford) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (canAfford) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${avatar.xpCost} XP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MathematicianDialog(
    avatar: Avatar,
    currentXP: Int,
    isSelected: Boolean,
    onDismiss: () -> Unit,
    onUnlock: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = !avatar.isUnlocked && currentXP >= avatar.xpCost
    val rarityColor = Color(avatar.rarity.color)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Portrait - Show emoji
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(rarityColor.copy(alpha = 0.2f))
                        .border(4.dp, rarityColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatar.emoji,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 64.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name and Era
                Text(
                    text = avatar.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = avatar.era,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rarity and Category
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = rarityColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = avatar.rarity.displayName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = avatar.category.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (avatar.isUnlocked) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Trivia sections
                    InfoSection(
                        icon = Icons.Default.Info,
                        title = "Fun Fact",
                        content = avatar.trivia
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoSection(
                        icon = Icons.Default.EmojiEvents,
                        title = "Major Contribution",
                        content = avatar.contribution
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoSection(
                        icon = Icons.Default.Lightbulb,
                        title = "Did You Know?",
                        content = avatar.funFact
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!avatar.isUnlocked) {
                        Button(
                            onClick = onUnlock,
                            modifier = Modifier.weight(1f),
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock (${avatar.xpCost} XP)")
                        }
                    } else if (!isSelected) {
                        Button(
                            onClick = onSelect,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select")
                        }
                    } else {
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Currently Selected", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
    }
}
