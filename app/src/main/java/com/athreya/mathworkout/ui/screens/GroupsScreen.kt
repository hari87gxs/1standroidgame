package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.social.Group
import com.athreya.mathworkout.viewmodel.GroupUiState
import com.athreya.mathworkout.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    groupViewModel: GroupViewModel,
    onNavigateBack: () -> Unit,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by groupViewModel.uiState.collectAsState()
    val myGroups by groupViewModel.myGroups.collectAsState()
    val publicGroups by groupViewModel.publicGroups.collectAsState()
    
    val context = LocalContext.current
    val userPreferences = remember { UserPreferencesManager(context) }
    var playerName by remember { mutableStateOf(userPreferences.getPlayerName()) }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showPublicGroupsDialog by remember { mutableStateOf(false) }
    var showPlayerNameDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<GroupAction?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Function to check if player name is set before performing actions
    fun requirePlayerName(action: GroupAction) {
        if (playerName.isNullOrBlank()) {
            pendingAction = action
            showPlayerNameDialog = true
        } else {
            when (action) {
                GroupAction.CREATE -> showCreateDialog = true
                GroupAction.JOIN -> showJoinDialog = true
                GroupAction.DISCOVER -> showPublicGroupsDialog = true
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        groupViewModel.syncGroupsFromFirebase()
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { requirePlayerName(GroupAction.DISCOVER) }) {
                        Icon(Icons.Default.Public, "Discover Groups")
                    }
                    IconButton(onClick = { requirePlayerName(GroupAction.JOIN) }) {
                        Icon(Icons.Default.QrCode, "Join Group")
                    }
                    IconButton(onClick = { requirePlayerName(GroupAction.CREATE) }) {
                        Icon(Icons.Default.Add, "Create Group")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { requirePlayerName(GroupAction.CREATE) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Create Group")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading or error state
            when (uiState) {
                is GroupUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GroupUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = (uiState as GroupUiState.Error).message,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {}
            }
            
            // Groups list
            if (myGroups.isEmpty()) {
                EmptyGroupsView(
                    onCreateClick = { showCreateDialog = true },
                    onJoinClick = { showJoinDialog = true },
                    onDiscoverClick = { showPublicGroupsDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myGroups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onGroupClick(group.groupId) }
                        )
                    }
                }
            }
        }
    }
    
    // Create Group Dialog
    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description, isPublic ->
                groupViewModel.createGroup(
                    groupName = name,
                    groupDescription = description,
                    isPublic = isPublic,
                    onSuccess = { group ->
                        showCreateDialog = false
                        onGroupClick(group.groupId)
                    },
                    onError = { error ->
                        // Error is already shown via uiState
                    }
                )
            }
        )
    }
    
    // Join Group Dialog
    if (showJoinDialog) {
        JoinGroupDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { groupCode ->
                groupViewModel.joinGroupByCode(
                    groupCode = groupCode,
                    onSuccess = { group ->
                        showJoinDialog = false
                        onGroupClick(group.groupId)
                    },
                    onError = { error ->
                        // Error is already shown via uiState
                    }
                )
            }
        )
    }
    
    // Public Groups Dialog
    if (showPublicGroupsDialog) {
        // Refresh public groups from Firebase when dialog opens
        LaunchedEffect(showPublicGroupsDialog) {
            if (showPublicGroupsDialog) {
                groupViewModel.loadPublicGroups()
            }
        }
        
        PublicGroupsDialog(
            publicGroups = publicGroups,
            onDismiss = { showPublicGroupsDialog = false },
            onGroupClick = { group ->
                showPublicGroupsDialog = false
                // Try to join the group
                groupViewModel.joinGroupByCode(
                    groupCode = group.groupCode,
                    onSuccess = { onGroupClick(group.groupId) },
                    onError = { /* Error shown via uiState */ }
                )
            }
        )
    }
    
    // Player Name Dialog
    if (showPlayerNameDialog) {
        com.athreya.mathworkout.ui.components.PlayerNameDialog(
            onDismiss = { 
                showPlayerNameDialog = false
                pendingAction = null
            },
            onConfirm = { name ->
                userPreferences.setPlayerName(name)
                playerName = name
                showPlayerNameDialog = false
                
                // Execute pending action
                pendingAction?.let { action ->
                    when (action) {
                        GroupAction.CREATE -> showCreateDialog = true
                        GroupAction.JOIN -> showJoinDialog = true
                        GroupAction.DISCOVER -> showPublicGroupsDialog = true
                    }
                }
                pendingAction = null
            }
        )
    }
}

@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (group.isPublic) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Public",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (group.groupDescription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.groupDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${group.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Code: ${group.groupCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Group",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyGroupsView(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Groups Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create or join a group to compete with friends!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Group")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Join with Code")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onDiscoverClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Public, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Discover Public Groups")
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                    Text("Make group public (discoverable)")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(groupName, groupDescription, isPublic) },
                enabled = groupName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var groupCode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Group") },
        text = {
            Column {
                Text(
                    text = "Enter the 6-digit group code to join",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = groupCode,
                    onValueChange = { if (it.length <= 6) groupCode = it.filter { c -> c.isDigit() } },
                    label = { Text("Group Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("123456") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(groupCode) },
                enabled = groupCode.length == 6
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicGroupsDialog(
    publicGroups: List<Group>,
    onDismiss: () -> Unit,
    onGroupClick: (Group) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text("Public Groups") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                )
                
                if (publicGroups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No public groups available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(publicGroups) { group ->
                            GroupCard(
                                group = group,
                                onClick = { onGroupClick(group) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Enum for group actions that require player name
 */
private enum class GroupAction {
    CREATE,
    JOIN,
    DISCOVER
}
