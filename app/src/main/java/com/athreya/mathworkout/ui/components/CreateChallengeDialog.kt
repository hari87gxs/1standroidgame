package com.athreya.mathworkout.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.social.GroupMember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeDialog(
    groupMembers: List<GroupMember>,
    currentMemberId: String,
    onDismiss: () -> Unit,
    onCreateChallenge: (
        opponentId: String,
        opponentName: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        questionCount: Int
    ) -> Unit
) {
    var selectedOpponent by remember { mutableStateOf<GroupMember?>(null) }
    var selectedGameMode by remember { mutableStateOf<GameMode>(GameMode.ADDITION_SUBTRACTION) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty>(Difficulty.EASY) }
    var selectedQuestionCount by remember { mutableStateOf<Int>(10) }
    var showOpponentPicker by remember { mutableStateOf<Boolean>(false) }
    
    // Filter out current user from opponent list
    val availableOpponents = groupMembers.filter { it.memberId != currentMemberId }
    
    // Debug logging
    android.util.Log.d("CreateChallengeDialog", "Total members: ${groupMembers.size}, Current ID: $currentMemberId, Available opponents: ${availableOpponents.size}")
    groupMembers.forEach { member ->
        android.util.Log.d("CreateChallengeDialog", "Member: ${member.memberName} (${member.memberId})")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Challenge") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Opponent Selection
                Text(
                    "Select Opponent",
                    style = MaterialTheme.typography.titleSmall
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOpponentPicker = true }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedOpponent?.memberName ?: "Choose opponent...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedOpponent == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                
                // Game Mode Selection
                Text(
                    "Game Mode",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameMode.values().forEach { mode ->
                        FilterChip(
                            selected = selectedGameMode == mode,
                            onClick = { selectedGameMode = mode },
                            label = { 
                                Text(
                                    mode.name.replace("_", " "),
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            leadingIcon = if (selectedGameMode == mode) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Difficulty Selection
                Text(
                    "Difficulty",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { 
                                Text(
                                    diff.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            },
                            leadingIcon = if (selectedDifficulty == diff) {
                                { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Question Count Selection
                Text(
                    "Number of Questions",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { count ->
                        FilterChip(
                            selected = selectedQuestionCount == count,
                            onClick = { selectedQuestionCount = count },
                            label = { Text("$count") },
                            leadingIcon = if (selectedQuestionCount == count) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    android.util.Log.d("CreateChallengeDialog", "Create button clicked, selectedOpponent=${selectedOpponent?.memberName}")
                    selectedOpponent?.let { opponent ->
                        android.util.Log.d("CreateChallengeDialog", "Calling onCreateChallenge with opponent: ${opponent.memberName} (${opponent.memberId})")
                        onCreateChallenge(
                            opponent.memberId,
                            opponent.memberName,
                            selectedGameMode,
                            selectedDifficulty,
                            selectedQuestionCount
                        )
                    } ?: run {
                        android.util.Log.e("CreateChallengeDialog", "No opponent selected!")
                    }
                },
                enabled = selectedOpponent != null
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
    
    // Opponent Picker Dialog
    if (showOpponentPicker) {
        AlertDialog(
            onDismissRequest = { showOpponentPicker = false },
            title = { Text("Choose Opponent") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableOpponents.isEmpty()) {
                        item {
                            Column {
                                Text(
                                    "No other members in this group",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Total members: ${groupMembers.size}, Your ID: $currentMemberId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(availableOpponents) { member ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOpponent = member
                                        showOpponentPicker = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedOpponent?.memberId == member.memberId) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
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
                                    Column {
                                        Text(
                                            text = member.memberName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Score: ${member.totalScore} • Games: ${member.gamesPlayed}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (selectedOpponent?.memberId == member.memberId) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpponentPicker = false }) {
                    Text("Done")
                }
            }
        )
    }
}
