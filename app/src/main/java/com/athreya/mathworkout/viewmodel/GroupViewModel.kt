package com.athreya.mathworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.AppDatabase
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.repository.GroupRepository
import com.athreya.mathworkout.data.social.Group
import com.athreya.mathworkout.data.social.GroupFirebaseService
import com.athreya.mathworkout.data.social.GroupMember
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing groups
 */
class GroupViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val groupDao = database.groupDao()
    private val groupMemberDao = database.groupMemberDao()
    private val userPreferences = UserPreferencesManager(application)
    
    private val firebaseService = GroupFirebaseService(
        FirebaseFirestore.getInstance(),
        application
    )
    
    private val repository = GroupRepository(
        groupDao,
        groupMemberDao,
        userPreferences,
        firebaseService
    )
    
    // UI State
    private val _uiState = MutableStateFlow<GroupUiState>(GroupUiState.Loading)
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()
    
    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups: StateFlow<List<Group>> = _myGroups.asStateFlow()
    
    private val _publicGroups = MutableStateFlow<List<Group>>(emptyList())
    val publicGroups: StateFlow<List<Group>> = _publicGroups.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<Group?>(null)
    val selectedGroup: StateFlow<Group?> = _selectedGroup.asStateFlow()
    
    private val _groupMembers = MutableStateFlow<List<GroupMember>>(emptyList())
    val groupMembers: StateFlow<List<GroupMember>> = _groupMembers.asStateFlow()
    
    init {
        loadMyGroups()
        loadPublicGroups()
    }
    
    /**
     * Load groups the user is a member of
     */
    private fun loadMyGroups() {
        viewModelScope.launch {
            repository.getMyGroups().collect { groups ->
                val memberId = userPreferences.getDeviceId()
                // Filter to only groups where user is a member
                val myGroupsList = groups.filter { group ->
                    viewModelScope.launch {
                        repository.isMemberOfGroup(group.groupId)
                    }
                    true // Temporary, need proper filtering
                }
                _myGroups.value = groups
            }
        }
    }
    
    /**
     * Load public groups
     */
    fun loadPublicGroups() {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            val result = firebaseService.getPublicGroups(50)
            if (result.isSuccess) {
                _publicGroups.value = result.getOrNull() ?: emptyList()
                _uiState.value = GroupUiState.Success
            } else {
                _uiState.value = GroupUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load public groups")
            }
        }
    }
    
    /**
     * Create a new group
     */
    fun createGroup(
        groupName: String,
        groupDescription: String,
        isPublic: Boolean,
        onSuccess: (Group) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            
            val result = repository.createGroup(groupName, groupDescription, isPublic)
            
            if (result.isSuccess) {
                val group = result.getOrNull()!!
                _uiState.value = GroupUiState.Success
                onSuccess(group)
                loadMyGroups()
            } else {
                _uiState.value = GroupUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create group")
                onError(result.exceptionOrNull()?.message ?: "Failed to create group")
            }
        }
    }
    
    /**
     * Join a group by code
     */
    fun joinGroupByCode(
        groupCode: String,
        onSuccess: (Group) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            
            // Repository now handles both local and Firebase lookup
            val result = repository.joinGroupByCode(groupCode)
            
            if (result.isSuccess) {
                val group = result.getOrNull()!!
                _uiState.value = GroupUiState.Success
                onSuccess(group)
                loadMyGroups()
            } else {
                _uiState.value = GroupUiState.Error(result.exceptionOrNull()?.message ?: "Failed to join group")
                onError(result.exceptionOrNull()?.message ?: "Failed to join group")
            }
        }
    }
    
    /**
     * Load a specific group and its members
     */
    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            // Sync group and members from Firebase first
            repository.syncGroupFromFirebase(groupId)
            
            // Launch separate coroutines to collect flows
            launch {
                // Load group details
                repository.getGroup(groupId).collect { group ->
                    _selectedGroup.value = group
                }
            }
            
            launch {
                // Load group members
                repository.getGroupMembers(groupId).collect { members ->
                    _groupMembers.value = members
                }
            }
        }
    }
    
    /**
     * Get members for a specific group as a Flow
     * This ensures each group shows only its own members
     */
    fun getGroupMembersFlow(groupId: String): Flow<List<GroupMember>> {
        return repository.getGroupMembers(groupId)
    }
    
    /**
     * Sync group members from Firebase
     */
    suspend fun syncGroupFromFirebase(groupId: String) {
        repository.syncGroupFromFirebase(groupId)
        // Reload members after sync
        repository.getGroupMembers(groupId).collect { members ->
            _groupMembers.value = members
        }
    }
    
    /**
     * Leave a group
     */
    fun leaveGroup(
        groupId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            
            val result = repository.leaveGroup(groupId)
            
            if (result.isSuccess) {
                _uiState.value = GroupUiState.Success
                onSuccess()
                loadMyGroups()
            } else {
                _uiState.value = GroupUiState.Error(result.exceptionOrNull()?.message ?: "Failed to leave group")
                onError(result.exceptionOrNull()?.message ?: "Failed to leave group")
            }
        }
    }
    
    /**
     * Delete a group (creator only)
     */
    fun deleteGroup(
        groupId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            
            val result = repository.deleteGroup(groupId)
            
            if (result.isSuccess) {
                _uiState.value = GroupUiState.Success
                onSuccess()
                loadMyGroups()
            } else {
                _uiState.value = GroupUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete group")
                onError(result.exceptionOrNull()?.message ?: "Failed to delete group")
            }
        }
    }
    
    /**
     * Get current user's membership in a group
     */
    fun getMyMembership(groupId: String): kotlinx.coroutines.flow.Flow<GroupMember?> {
        return repository.getMyMembership(groupId)
    }
    
    /**
     * Sync all groups from Firebase and refresh the UI
     * This will remove deleted groups and update all group data
     */
    fun syncGroupsFromFirebase() {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading
            try {
                repository.syncAllGroupsFromFirebase()
                loadMyGroups()
                loadPublicGroups()
                _uiState.value = GroupUiState.Success
            } catch (e: Exception) {
                android.util.Log.e("GroupViewModel", "Error syncing groups: ${e.message}", e)
                _uiState.value = GroupUiState.Error(e.message ?: "Failed to sync groups")
            }
        }
    }
    
    /**
     * Update member stats after completing a game
     * Updates scores in all groups the user is a member of
     */
    fun updateMemberStatsAfterGame(score: Int) {
        viewModelScope.launch {
            try {
                // Get all groups user is a member of
                val groups = repository.getMyGroups().first()
                
                // Update stats in each group
                for (group in groups) {
                    repository.updateMemberStatsAfterGame(group.groupId, score)
                    android.util.Log.d("GroupViewModel", "Updated stats in group ${group.groupName}: +$score points")
                }
            } catch (e: Exception) {
                android.util.Log.e("GroupViewModel", "Error updating member stats: ${e.message}", e)
            }
        }
    }
    
}

/**
 * UI State for group operations
 */
sealed class GroupUiState {
    object Loading : GroupUiState()
    object Success : GroupUiState()
    data class Error(val message: String) : GroupUiState()
}
