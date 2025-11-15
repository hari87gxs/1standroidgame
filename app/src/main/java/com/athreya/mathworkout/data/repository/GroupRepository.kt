package com.athreya.mathworkout.data.repository

import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.social.Group
import com.athreya.mathworkout.data.social.GroupDao
import com.athreya.mathworkout.data.social.GroupFirebaseService
import com.athreya.mathworkout.data.social.GroupMember
import com.athreya.mathworkout.data.social.GroupMemberDao
import com.athreya.mathworkout.data.social.MemberRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Repository for managing groups and group membership
 * Handles business logic for creating, joining, and managing groups
 * Syncs with Firebase when available
 */
class GroupRepository(
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val userPreferences: UserPreferencesManager,
    private val firebaseService: GroupFirebaseService? = null
) {
    
    /**
     * Create a new group
     * @param groupName Name of the group
     * @param groupDescription Description of the group
     * @param isPublic Whether the group can be discovered publicly
     * @return Result with the created Group or error
     */
    suspend fun createGroup(
        groupName: String,
        groupDescription: String = "",
        isPublic: Boolean = false
    ): Result<Group> {
        return try {
            val creatorId = userPreferences.getDeviceId()
            val creatorName = userPreferences.getPlayerName() ?: "Player"
            
            // Generate unique group ID and code
            val groupId = UUID.randomUUID().toString()
            val groupCode = Group.generateGroupCode()
            
            // Create group
            val group = Group(
                groupId = groupId,
                groupName = groupName,
                groupDescription = groupDescription,
                creatorId = creatorId,
                creatorName = creatorName,
                groupCode = groupCode,
                isPublic = isPublic,
                memberCount = 1
            )
            
            // Create creator as first member
            val creatorMember = GroupMember(
                groupId = groupId,
                memberId = creatorId,
                memberName = creatorName,
                role = MemberRole.CREATOR
            )
            
            // Insert both
            groupDao.insertGroup(group)
            groupMemberDao.insertMember(creatorMember)
            
            // Sync to Firebase - MUST complete successfully
            if (firebaseService != null) {
                val groupUploadResult = firebaseService.uploadGroup(group)
                val memberUploadResult = firebaseService.uploadMember(creatorMember)
                
                android.util.Log.d("GroupRepository", "Group upload result: ${groupUploadResult.isSuccess}")
                android.util.Log.d("GroupRepository", "Member upload result: ${memberUploadResult.isSuccess}")
                
                if (groupUploadResult.isFailure) {
                    android.util.Log.e("GroupRepository", "Failed to upload group: ${groupUploadResult.exceptionOrNull()?.message}")
                }
                if (memberUploadResult.isFailure) {
                    android.util.Log.e("GroupRepository", "Failed to upload member: ${memberUploadResult.exceptionOrNull()?.message}")
                }
            } else {
                android.util.Log.e("GroupRepository", "Firebase service is NULL - cannot upload group!")
            }
            
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Join a group using a group code
     * @param groupCode 6-digit group code
     * @return Result with the joined Group or error
     */
    suspend fun joinGroupByCode(groupCode: String): Result<Group> {
        return try {
            val memberId = userPreferences.getDeviceId()
            val memberName = userPreferences.getPlayerName() ?: "Player"
            
            // Find group by code in local database first
            var group = groupDao.getGroupByCode(groupCode)
            
            // If not found locally, try to find in Firebase
            if (group == null && firebaseService != null) {
                val firebaseResult = firebaseService.findGroupByCode(groupCode)
                if (firebaseResult.isFailure) {
                    return Result.failure(Exception("Group not found with code: $groupCode"))
                }
                group = firebaseResult.getOrNull()!!
                // Save the group to local database
                groupDao.insertGroup(group)
            }
            
            // If still not found, return error
            if (group == null) {
                return Result.failure(Exception("Group not found with code: $groupCode"))
            }
            
            // Check if already a member
            val existingMember = groupMemberDao.getMember(group.groupId, memberId)
            if (existingMember != null) {
                return Result.failure(Exception("You are already a member of this group"))
            }
            
            // Check if group is full
            if (group.memberCount >= group.maxMembers) {
                return Result.failure(Exception("Group is full (${group.maxMembers} members)"))
            }
            
            // Add as member
            val member = GroupMember(
                groupId = group.groupId,
                memberId = memberId,
                memberName = memberName,
                role = MemberRole.MEMBER
            )
            
            groupMemberDao.insertMember(member)
            
            // Upload member to Firebase
            if (firebaseService != null) {
                firebaseService.uploadMember(member)
            }
            
            // Update member count
            val newCount = group.memberCount + 1
            groupDao.updateMemberCount(group.groupId, newCount)
            
            // Update group member count in Firebase
            if (firebaseService != null) {
                val updatedGroup = group.copy(memberCount = newCount)
                groupDao.insertGroup(updatedGroup)
                firebaseService.uploadGroup(updatedGroup)
            }
            
            Result.success(group.copy(memberCount = newCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Leave a group
     * @param groupId ID of the group to leave
     * @return Result with success or error
     */
    suspend fun leaveGroup(groupId: String): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val group = groupDao.getGroupById(groupId)
                ?: return Result.failure(Exception("Group not found"))
            
            val member = groupMemberDao.getMember(groupId, memberId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            // Creators cannot leave, they must delete the group
            if (member.role == MemberRole.CREATOR) {
                return Result.failure(Exception("Group creator cannot leave. Delete the group instead."))
            }
            
            // Remove member locally
            groupMemberDao.removeMember(groupId, memberId)
            
            // Update member count locally
            val newCount = group.memberCount - 1
            groupDao.updateMemberCount(groupId, newCount)
            
            // Sync to Firebase
            if (firebaseService != null) {
                // Delete member from Firebase
                firebaseService.deleteMember(groupId, memberId)
                
                // Update group with new member count
                val updatedGroup = group.copy(memberCount = newCount)
                groupDao.insertGroup(updatedGroup)
                firebaseService.uploadGroup(updatedGroup)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a group (creator only)
     * @param groupId ID of the group to delete
     * @return Result with success or error
     */
    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val member = groupMemberDao.getMember(groupId, memberId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            // Only creator can delete
            if (member.role != MemberRole.CREATOR) {
                return Result.failure(Exception("Only the group creator can delete the group"))
            }
            
            // Delete all members first (local)
            groupMemberDao.deleteAllGroupMembers(groupId)
            
            // Delete the group (local)
            groupDao.deleteGroupById(groupId)
            
            // Sync to Firebase
            if (firebaseService != null) {
                firebaseService.deleteGroup(groupId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a member from the group (creator/admin only)
     * @param groupId ID of the group
     * @param memberIdToRemove ID of the member to remove
     * @return Result with success or error
     */
    suspend fun removeMember(groupId: String, memberIdToRemove: String): Result<Unit> {
        return try {
            val requesterId = userPreferences.getDeviceId()
            
            val requester = groupMemberDao.getMember(groupId, requesterId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            // Only creator and admin can remove members
            if (requester.role != MemberRole.CREATOR && requester.role != MemberRole.ADMIN) {
                return Result.failure(Exception("Only creators and admins can remove members"))
            }
            
            val memberToRemove = groupMemberDao.getMember(groupId, memberIdToRemove)
                ?: return Result.failure(Exception("Member not found in group"))
            
            // Cannot remove the creator
            if (memberToRemove.role == MemberRole.CREATOR) {
                return Result.failure(Exception("Cannot remove the group creator"))
            }
            
            // Admins cannot remove other admins
            if (requester.role == MemberRole.ADMIN && memberToRemove.role == MemberRole.ADMIN) {
                return Result.failure(Exception("Admins cannot remove other admins"))
            }
            
            // Remove member locally
            groupMemberDao.removeMember(groupId, memberIdToRemove)
            
            // Update member count locally
            val group = groupDao.getGroupById(groupId)
            if (group != null) {
                val newCount = group.memberCount - 1
                groupDao.updateMemberCount(groupId, newCount)
                
                // Sync to Firebase
                if (firebaseService != null) {
                    // Delete member from Firebase
                    firebaseService.deleteMember(groupId, memberIdToRemove)
                    
                    // Update group with new member count
                    val updatedGroup = group.copy(memberCount = newCount)
                    groupDao.insertGroup(updatedGroup)
                    firebaseService.uploadGroup(updatedGroup)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Promote a member to admin (creator only)
     * @param groupId ID of the group
     * @param memberIdToPromote ID of the member to promote
     * @return Result with success or error
     */
    suspend fun promoteToAdmin(groupId: String, memberIdToPromote: String): Result<Unit> {
        return try {
            val requesterId = userPreferences.getDeviceId()
            
            val requester = groupMemberDao.getMember(groupId, requesterId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            // Only creator can promote
            if (requester.role != MemberRole.CREATOR) {
                return Result.failure(Exception("Only the group creator can promote members"))
            }
            
            val memberToPromote = groupMemberDao.getMember(groupId, memberIdToPromote)
                ?: return Result.failure(Exception("Member not found in group"))
            
            // Update role to admin locally
            val updatedMember = memberToPromote.copy(role = MemberRole.ADMIN)
            groupMemberDao.updateMember(updatedMember)
            
            // Sync to Firebase
            if (firebaseService != null) {
                firebaseService.uploadMember(updatedMember)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update member stats after a game
     * @param groupId ID of the group
     * @param score Score earned in the game
     */
    suspend fun updateMemberStatsAfterGame(groupId: String, score: Int) {
        try {
            val memberId = userPreferences.getDeviceId()
            val timestamp = System.currentTimeMillis()
            
            // Check if member exists in this group
            val member = groupMemberDao.getMember(groupId, memberId)
            if (member == null) {
                android.util.Log.w("GroupRepository", "Member $memberId not found in group $groupId, skipping stats update")
                return
            }
            
            android.util.Log.d("GroupRepository", "Updating stats for member $memberId in group $groupId: +$score points")
            
            // Update locally
            groupMemberDao.updateMemberStats(groupId, memberId, score, timestamp)
            
            // Verify update
            val updatedMember = groupMemberDao.getMember(groupId, memberId)
            android.util.Log.d("GroupRepository", "After update: totalScore=${updatedMember?.totalScore}, gamesPlayed=${updatedMember?.gamesPlayed}")
            
            // Sync to Firebase
            if (firebaseService != null && updatedMember != null) {
                firebaseService.uploadMember(updatedMember)
                android.util.Log.d("GroupRepository", "Uploaded updated member to Firebase")
            }
        } catch (e: Exception) {
            // Log error but don't fail
            android.util.Log.e("GroupRepository", "Error updating member stats", e)
        }
    }
    
    /**
     * Get all groups the current user is a member of
     */
    fun getMyGroups(): Flow<List<Group>> {
        // This will be implemented with a query that joins groups and members
        // For now, we'll need to filter in the ViewModel
        return groupDao.getAllGroups()
    }
    
    /**
     * Get a specific group by ID
     */
    fun getGroup(groupId: String): Flow<Group?> {
        return groupDao.getGroupByIdFlow(groupId)
    }
    
    /**
     * Sync group and its members from Firebase
     */
    suspend fun syncGroupFromFirebase(groupId: String) {
        if (firebaseService == null) {
            android.util.Log.w("GroupRepository", "Firebase service is null, cannot sync group from Firebase")
            return
        }
        
        try {
            android.util.Log.d("GroupRepository", "Syncing group $groupId from Firebase...")
            
            // First, sync the group itself to get updated member count
            val groupResult = firebaseService.downloadGroup(groupId)
            if (groupResult.isSuccess) {
                val group = groupResult.getOrNull()
                if (group != null) {
                    groupDao.insertGroup(group)
                    android.util.Log.d("GroupRepository", "Updated group from Firebase: memberCount=${group.memberCount}")
                }
            }
            
            // Then sync group members from Firebase
            val membersResult = firebaseService.downloadGroupMembers(groupId)
            if (membersResult.isSuccess) {
                val members = membersResult.getOrNull() ?: emptyList()
                android.util.Log.d("GroupRepository", "Downloaded ${members.size} members from Firebase for group $groupId")
                members.forEach { member ->
                    groupMemberDao.insertMember(member)
                    android.util.Log.d("GroupRepository", "Inserted member: ${member.memberName} (${member.memberId}) in group $groupId")
                }
            } else {
                android.util.Log.e("GroupRepository", "Failed to download members: ${membersResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupRepository", "Error syncing group from Firebase", e)
        }
    }
    
    /**
     * Sync all user's groups from Firebase and remove deleted groups
     * This refreshes the entire group list from Firebase
     */
    suspend fun syncAllGroupsFromFirebase() {
        if (firebaseService == null) {
            android.util.Log.w("GroupRepository", "Firebase service is null, cannot sync groups")
            return
        }
        
        try {
            val memberId = userPreferences.getDeviceId()
            android.util.Log.d("GroupRepository", "Syncing all groups for member $memberId from Firebase...")
            
            // Get all local groups where user is a member
            val localGroups = groupDao.getAllGroupsSync()
            val localGroupIds = localGroups.map { it.groupId }.toSet()
            
            // Get user's memberships from Firebase
            val membershipsResult = firebaseService.downloadMembershipsByUser(memberId)
            if (membershipsResult.isSuccess) {
                val memberships = membershipsResult.getOrNull() ?: emptyList()
                val firebaseGroupIds = memberships.map { it.groupId }.toSet()
                
                android.util.Log.d("GroupRepository", "Found ${memberships.size} memberships in Firebase")
                
                // Sync each group that exists in Firebase
                for (membership in memberships) {
                    syncGroupFromFirebase(membership.groupId)
                }
                
                // Remove groups that exist locally but not in Firebase (deleted groups)
                val deletedGroupIds = localGroupIds - firebaseGroupIds
                for (groupId in deletedGroupIds) {
                    android.util.Log.d("GroupRepository", "Removing deleted group $groupId from local database")
                    groupDao.deleteGroupById(groupId)
                    groupMemberDao.deleteAllGroupMembers(groupId)
                }
                
                android.util.Log.d("GroupRepository", "Sync complete. Removed ${deletedGroupIds.size} deleted groups")
            } else {
                android.util.Log.e("GroupRepository", "Failed to download memberships: ${membershipsResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("GroupRepository", "Error syncing all groups from Firebase", e)
        }
    }
    
    /**
     * Get all members of a group (sorted by score)
     */
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>> {
        return groupMemberDao.getGroupMembers(groupId)
    }
    
    /**
     * Get top N members of a group
     */
    fun getTopMembers(groupId: String, limit: Int = 10): Flow<List<GroupMember>> {
        return groupMemberDao.getTopMembers(groupId, limit)
    }
    
    /**
     * Get current user's membership in a group
     */
    fun getMyMembership(groupId: String): Flow<GroupMember?> {
        val memberId = userPreferences.getDeviceId()
        return groupMemberDao.getMemberFlow(groupId, memberId)
    }
    
    /**
     * Get all public groups
     */
    fun getPublicGroups(): Flow<List<Group>> {
        return groupDao.getPublicGroups()
    }
    
    /**
     * Check if current user is a member of a group
     */
    suspend fun isMemberOfGroup(groupId: String): Boolean {
        val memberId = userPreferences.getDeviceId()
        return groupMemberDao.getMember(groupId, memberId) != null
    }
    
    /**
     * Get groups where current user is the creator
     */
    fun getMyCreatedGroups(): Flow<List<Group>> {
        val creatorId = userPreferences.getDeviceId()
        return groupDao.getGroupsCreatedBy(creatorId)
    }
    
    /**
     * Update group details (creator only)
     */
    suspend fun updateGroupDetails(
        groupId: String,
        newName: String? = null,
        newDescription: String? = null,
        newIsPublic: Boolean? = null
    ): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val member = groupMemberDao.getMember(groupId, memberId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            if (member.role != MemberRole.CREATOR) {
                return Result.failure(Exception("Only the group creator can update group details"))
            }
            
            val group = groupDao.getGroupById(groupId)
                ?: return Result.failure(Exception("Group not found"))
            
            val updatedGroup = group.copy(
                groupName = newName ?: group.groupName,
                groupDescription = newDescription ?: group.groupDescription,
                isPublic = newIsPublic ?: group.isPublic
            )
            
            // Update locally
            groupDao.updateGroup(updatedGroup)
            
            // Sync to Firebase
            if (firebaseService != null) {
                firebaseService.uploadGroup(updatedGroup)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
