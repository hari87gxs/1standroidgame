package com.athreya.mathworkout.data.social

import android.util.Log
import com.athreya.mathworkout.data.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages automatic syncing between local database and Firebase
 * Handles uploading local changes and downloading remote updates
 */
class SocialSyncManager(
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val challengeDao: ChallengeDao,
    private val groupFirebaseService: GroupFirebaseService,
    private val challengeFirebaseService: ChallengeFirebaseService,
    private val userPreferences: UserPreferencesManager
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isSyncing = false
    
    companion object {
        private const val TAG = "SocialSyncManager"
        private const val SYNC_INTERVAL_MS = 30_000L // 30 seconds
    }
    
    /**
     * Start periodic sync
     * Syncs local changes to Firebase and downloads remote updates
     */
    fun startPeriodicSync() {
        scope.launch {
            while (true) {
                if (!isSyncing) {
                    syncAll()
                }
                delay(SYNC_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Sync all data (groups, members, challenges)
     */
    suspend fun syncAll() {
        if (isSyncing) {
            Log.d(TAG, "Sync already in progress, skipping")
            return
        }
        
        isSyncing = true
        try {
            Log.d(TAG, "Starting sync...")
            
            // Upload unsynced data
            uploadUnsyncedGroups()
            uploadUnsyncedMembers()
            uploadUnsyncedChallenges()
            
            // Download updates for user's groups
            downloadGroupUpdates()
            
            Log.d(TAG, "Sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
        } finally {
            isSyncing = false
        }
    }
    
    /**
     * Upload unsynced groups to Firebase
     */
    private suspend fun uploadUnsyncedGroups() {
        try {
            val unsyncedGroups = groupDao.getUnsyncedGroups()
            Log.d(TAG, "Uploading ${unsyncedGroups.size} unsynced groups")
            
            for (group in unsyncedGroups) {
                val result = groupFirebaseService.uploadGroup(group)
                if (result.isSuccess) {
                    groupDao.updateSyncStatus(
                        group.groupId,
                        synced = true,
                        timestamp = System.currentTimeMillis()
                    )
                    Log.d(TAG, "Uploaded group: ${group.groupName}")
                } else {
                    Log.e(TAG, "Failed to upload group ${group.groupName}: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading groups: ${e.message}", e)
        }
    }
    
    /**
     * Upload unsynced members to Firebase
     */
    private suspend fun uploadUnsyncedMembers() {
        try {
            val unsyncedMembers = groupMemberDao.getUnsyncedMembers()
            Log.d(TAG, "Uploading ${unsyncedMembers.size} unsynced members")
            
            for (member in unsyncedMembers) {
                val result = groupFirebaseService.uploadMember(member)
                if (result.isSuccess) {
                    groupMemberDao.updateSyncStatus(
                        member.groupId,
                        member.memberId,
                        synced = true
                    )
                    Log.d(TAG, "Uploaded member: ${member.memberName} to group ${member.groupId}")
                } else {
                    Log.e(TAG, "Failed to upload member ${member.memberName}: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading members: ${e.message}", e)
        }
    }
    
    /**
     * Upload unsynced challenges to Firebase
     */
    private suspend fun uploadUnsyncedChallenges() {
        try {
            val unsyncedChallenges = challengeDao.getUnsyncedChallenges()
            Log.d(TAG, "Uploading ${unsyncedChallenges.size} unsynced challenges")
            
            for (challenge in unsyncedChallenges) {
                val result = challengeFirebaseService.uploadChallenge(challenge)
                if (result.isSuccess) {
                    challengeDao.updateSyncStatus(
                        challenge.challengeId,
                        synced = true,
                        timestamp = System.currentTimeMillis()
                    )
                    Log.d(TAG, "Uploaded challenge: ${challenge.challengeId}")
                } else {
                    Log.e(TAG, "Failed to upload challenge: ${result.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading challenges: ${e.message}", e)
        }
    }
    
    /**
     * Download updates for groups the user is a member of
     */
    private suspend fun downloadGroupUpdates() {
        try {
            val memberId = userPreferences.getDeviceId()
            val myMemberships = groupMemberDao.getMemberGroups(memberId)
            
            // This is a Flow, we need to collect it once
            // In a real implementation, you'd want to use first() or similar
            // For now, we'll just log that we'd download updates
            Log.d(TAG, "Would download updates for user's groups")
            
            // TODO: Implement proper Flow collection and update logic
            // This would involve:
            // 1. Collecting the Flow of user's group memberships
            // 2. For each group, downloading latest data from Firebase
            // 3. Updating local database with remote changes
            // 4. Handling conflicts (last-write-wins or more sophisticated)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading group updates: ${e.message}", e)
        }
    }
    
    /**
     * Sync a specific group immediately
     * @param groupId ID of the group to sync
     */
    suspend fun syncGroup(groupId: String) {
        try {
            Log.d(TAG, "Syncing group: $groupId")
            
            // Upload group
            val group = groupDao.getGroupById(groupId)
            if (group != null && !group.synced) {
                val result = groupFirebaseService.uploadGroup(group)
                if (result.isSuccess) {
                    groupDao.updateSyncStatus(groupId, synced = true, timestamp = System.currentTimeMillis())
                }
            }
            
            // Upload members
            val members = groupMemberDao.getGroupMembers(groupId)
            // Note: This is a Flow, in production you'd collect it properly
            
            // Download latest from Firebase
            val downloadResult = groupFirebaseService.downloadGroup(groupId)
            if (downloadResult.isSuccess) {
                val remoteGroup = downloadResult.getOrNull()
                if (remoteGroup != null) {
                    groupDao.insertGroup(remoteGroup)
                }
            }
            
            // Download members
            val membersResult = groupFirebaseService.downloadGroupMembers(groupId)
            if (membersResult.isSuccess) {
                val remoteMembers = membersResult.getOrNull() ?: emptyList()
                for (member in remoteMembers) {
                    groupMemberDao.insertMember(member)
                }
            }
            
            Log.d(TAG, "Group sync completed: $groupId")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing group $groupId: ${e.message}", e)
        }
    }
    
    /**
     * Sync a specific challenge immediately
     * @param challengeId ID of the challenge to sync
     */
    suspend fun syncChallenge(challengeId: String) {
        try {
            Log.d(TAG, "Syncing challenge: $challengeId")
            
            // Upload challenge
            val challenge = challengeDao.getChallengeById(challengeId)
            if (challenge != null && !challenge.synced) {
                val result = challengeFirebaseService.uploadChallenge(challenge)
                if (result.isSuccess) {
                    challengeDao.updateSyncStatus(challengeId, synced = true, timestamp = System.currentTimeMillis())
                }
            }
            
            // Download latest from Firebase
            val downloadResult = challengeFirebaseService.downloadChallenge(challengeId)
            if (downloadResult.isSuccess) {
                val remoteChallenge = downloadResult.getOrNull()
                if (remoteChallenge != null) {
                    challengeDao.insertChallenge(remoteChallenge)
                }
            }
            
            Log.d(TAG, "Challenge sync completed: $challengeId")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing challenge $challengeId: ${e.message}", e)
        }
    }
    
    /**
     * Mark a group as needing sync
     * @param groupId ID of the group
     */
    suspend fun markGroupForSync(groupId: String) {
        try {
            groupDao.updateSyncStatus(groupId, synced = false, timestamp = 0L)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking group for sync: ${e.message}", e)
        }
    }
    
    /**
     * Mark a challenge as needing sync
     * @param challengeId ID of the challenge
     */
    suspend fun markChallengeForSync(challengeId: String) {
        try {
            challengeDao.updateSyncStatus(challengeId, synced = false, timestamp = 0L)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking challenge for sync: ${e.message}", e)
        }
    }
    
    /**
     * Force immediate sync
     */
    fun forceSyncNow() {
        scope.launch {
            syncAll()
        }
    }
}
