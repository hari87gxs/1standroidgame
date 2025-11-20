package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.social.Group
import com.athreya.mathworkout.data.social.GroupMember
import com.athreya.mathworkout.data.social.MemberRole
import com.athreya.mathworkout.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupViewModel: GroupViewModel,
    challengeViewModel: com.athreya.mathworkout.viewmodel.ChallengeViewModel,
    onNavigateBack: () -> Unit,
    onCreateChallenge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedGroup by groupViewModel.selectedGroup.collectAsState()
    val groupMembers by groupViewModel.getGroupMembersFlow(groupId).collectAsState(initial = emptyList())
    val myMembership by groupViewModel.getMyMembership(groupId).collectAsState(initial = null)
    
    val context = LocalContext.current
    val userPreferences = remember { UserPreferencesManager(context) }
    val currentDeviceId = userPreferences.getDeviceId()
    val scope = rememberCoroutineScope()
    
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showCreateChallengeDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(groupId) {
        groupViewModel.loadGroup(groupId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedGroup?.groupName ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        groupViewModel.loadGroup(groupId)
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, "Share Code")
                    }
                    IconButton(onClick = {
                        if (myMembership?.role == MemberRole.CREATOR) {
                            showDeleteDialog = true
                        } else {
                            showLeaveDialog = true
                        }
                    }) {
                        Icon(
                            if (myMembership?.role == MemberRole.CREATOR) Icons.Default.Delete else Icons.Default.ExitToApp,
                            if (myMembership?.role == MemberRole.CREATOR) "Delete Group" else "Leave Group"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Sync members from Firebase and show dialog
                    showCreateChallengeDialog = true
                    scope.launch {
                        groupViewModel.syncGroupFromFirebase(groupId)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.EmojiEvents, "Create Challenge")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group Info Card
            selectedGroup?.let { group ->
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
                            Column {
                                Text(
                                    text = group.groupName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (group.groupDescription.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = group.groupDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            
                            if (group.isPublic) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = "Public Group",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            GroupStat(
                                icon = Icons.Default.People,
                                label = "Members",
                                value = group.memberCount.toString()
                            )
                            GroupStat(
                                icon = Icons.Default.QrCode,
                                label = "Group Code",
                                value = group.groupCode
                            )
                            myMembership?.let { membership ->
                                GroupStat(
                                    icon = Icons.Default.Star,
                                    label = "Role",
                                    value = membership.role.name.lowercase().capitalize()
                                )
                            }
                        }
                    }
                }
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Leaderboard") },
                    icon = { Icon(Icons.Default.EmojiEvents, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Challenges") },
                    icon = { Icon(Icons.Default.Whatshot, null) }
                )
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> LeaderboardTab(
                    members = groupMembers,
                    currentUserId = myMembership?.memberId ?: ""
                )
                1 -> ChallengesTab(
                    groupId = groupId,
                    onCreateChallenge = onCreateChallenge
                )
            }
        }
    }
    
    // Share Dialog
    if (showShareDialog) {
        ShareCodeDialog(
            groupCode = selectedGroup?.groupCode ?: "",
            groupName = selectedGroup?.groupName ?: "",
            onDismiss = { showShareDialog = false }
        )
    }
    
    // Create Challenge Dialog
    if (showCreateChallengeDialog) {
        com.athreya.mathworkout.ui.components.CreateChallengeDialog(
            groupMembers = groupMembers,
            currentMemberId = currentDeviceId,
            onDismiss = { 
                android.util.Log.d("GroupDetailScreen", "Create challenge dialog dismissed")
                showCreateChallengeDialog = false 
            },
            onCreateChallenge = { opponentId, opponentName, gameMode, difficulty, questionCount ->
                android.util.Log.d("GroupDetailScreen", "onCreateChallenge called: opponent=$opponentId ($opponentName), mode=$gameMode, difficulty=$difficulty, questions=$questionCount")
                challengeViewModel.createChallenge(
                    groupId = groupId,
                    challengedId = opponentId,
                    challengedName = opponentName,
                    gameMode = gameMode,
                    difficulty = difficulty,
                    questionCount = questionCount,
                    onSuccess = { 
                        android.util.Log.d("GroupDetailScreen", "Challenge created successfully")
                        showCreateChallengeDialog = false
                        onCreateChallenge(groupId)
                    },
                    onError = { error ->
                        android.util.Log.e("GroupDetailScreen", "Challenge creation failed: $error")
                        showCreateChallengeDialog = false
                    }
                )
            }
        )
    }
    
    // Leave Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group") },
            text = { Text("Are you sure you want to leave this group?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupViewModel.leaveGroup(
                            groupId = groupId,
                            onSuccess = {
                                showLeaveDialog = false
                                onNavigateBack()
                            },
                            onError = { showLeaveDialog = false }
                        )
                    }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupViewModel.deleteGroup(
                            groupId = groupId,
                            onSuccess = {
                                showDeleteDialog = false
                                onNavigateBack()
                            },
                            onError = { showDeleteDialog = false }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GroupStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LeaderboardTab(
    members: List<GroupMember>,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    if (members.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No members yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(members) { index, member ->
                LeaderboardCard(
                    rank = index + 1,
                    member = member,
                    isCurrentUser = member.memberId == currentUserId
                )
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    member: GroupMember,
    isCurrentUser: Boolean
) {
    // Load badges for current user
    val context = androidx.compose.ui.platform.LocalContext.current
    val badgeManager = remember { com.athreya.mathworkout.data.BadgeManager(com.athreya.mathworkout.data.UserPreferencesManager(context)) }
    val unlockedBadges = remember { 
        if (isCurrentUser) badgeManager.getUnlockedBadges().take(3) else emptyList() 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentUser) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
                rank == 1 -> Color(0xFFFFD700).copy(alpha = 0.1f) // Gold
                rank == 2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f) // Silver
                rank == 3 -> Color(0xFFCD7F32).copy(alpha = 0.1f) // Bronze
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank or Medal
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (rank) {
                        1 -> Text("🥇", style = MaterialTheme.typography.headlineMedium)
                        2 -> Text("🥈", style = MaterialTheme.typography.headlineMedium)
                        3 -> Text("🥉", style = MaterialTheme.typography.headlineMedium)
                        else -> Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.memberName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(You)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (member.role != MemberRole.MEMBER) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge {
                                Text(
                                    text = member.role.name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    // Display badges for current user
                    if (isCurrentUser && unlockedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        com.athreya.mathworkout.ui.components.BadgeRow(
                            badges = unlockedBadges,
                            maxBadges = 3
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${member.gamesPlayed} games",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${member.challengesWon}W-${member.challengesLost}L",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Text(
                text = member.totalScore.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChallengesTab(
    groupId: String,
    onCreateChallenge: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Whatshot,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Challenges",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View all challenges and compete with group members",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onCreateChallenge(groupId) }) {
                Icon(Icons.Default.EmojiEvents, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go to Challenges")
            }
        }
    }
}

@Composable
fun ShareCodeDialog(
    groupCode: String,
    groupName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Group Code") },
        text = {
            Column {
                Text("Share this code with friends to join:")
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = groupCode,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

