package com.athreya.mathworkout.data.social

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase service for syncing groups and members to Firestore
 * 
 * Firestore Structure:
 * - Collection: "groups"
 *   - Document ID: groupId
 *   - Fields: groupName, groupDescription, creatorId, creatorName, groupCode, isPublic, etc.
 *   - SubCollection: "members"
 *     - Document ID: memberId
 *     - Fields: memberName, role, joinedAt, totalScore, etc.
 */
class GroupFirebaseService(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    companion object {
        private const val COLLECTION_GROUPS = "groups"
        private const val COLLECTION_MEMBERS = "members"
        private const val FIELD_GROUP_CODE = "groupCode"
        private const val FIELD_IS_PUBLIC = "isPublic"
        private const val FIELD_MEMBER_COUNT = "memberCount"
        private const val FIELD_TOTAL_SCORE = "totalScore"
    }
    
    /**
     * Upload a group to Firestore
     * @param group Group to upload
     * @return Result with success or error
     */
    suspend fun uploadGroup(group: Group): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val groupData = mapOf(
                "groupId" to group.groupId,
                "groupName" to group.groupName,
                "groupDescription" to group.groupDescription,
                "creatorId" to group.creatorId,
                "creatorName" to group.creatorName,
                "groupCode" to group.groupCode,
                "isPublic" to group.isPublic,
                "createdAt" to group.createdAt,
                "memberCount" to group.memberCount,
                "maxMembers" to group.maxMembers,
                "groupIcon" to group.groupIcon,
                "lastUpdatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_GROUPS)
                .document(group.groupId)
                .set(groupData, SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload a group member to Firestore
     * @param member GroupMember to upload
     * @return Result with success or error
     */
    suspend fun uploadMember(member: GroupMember): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val memberData = mapOf(
                "memberId" to member.memberId,
                "memberName" to member.memberName,
                "role" to member.role.name,
                "joinedAt" to member.joinedAt,
                "totalScore" to member.totalScore,
                "gamesPlayed" to member.gamesPlayed,
                "challengesWon" to member.challengesWon,
                "challengesLost" to member.challengesLost,
                "lastActiveAt" to member.lastActiveAt,
                "isActive" to member.isActive
            )
            
            firestore.collection(COLLECTION_GROUPS)
                .document(member.groupId)
                .collection(COLLECTION_MEMBERS)
                .document(member.memberId)
                .set(memberData, SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download a group from Firestore by ID
     * @param groupId ID of the group
     * @return Result with Group or error
     */
    suspend fun downloadGroup(groupId: String): Result<Group> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val document = firestore.collection(COLLECTION_GROUPS)
                .document(groupId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Group not found"))
            }
            
            val group = Group(
                groupId = document.getString("groupId") ?: groupId,
                groupName = document.getString("groupName") ?: "",
                groupDescription = document.getString("groupDescription") ?: "",
                creatorId = document.getString("creatorId") ?: "",
                creatorName = document.getString("creatorName") ?: "",
                groupCode = document.getString("groupCode") ?: "",
                isPublic = document.getBoolean("isPublic") ?: false,
                createdAt = document.getLong("createdAt") ?: 0L,
                memberCount = document.getLong("memberCount")?.toInt() ?: 1,
                maxMembers = document.getLong("maxMembers")?.toInt() ?: 50,
                groupIcon = document.getString("groupIcon"),
                synced = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Find a group by its join code
     * @param groupCode 6-digit group code
     * @return Result with Group or error
     */
    suspend fun findGroupByCode(groupCode: String): Result<Group> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val querySnapshot = firestore.collection(COLLECTION_GROUPS)
                .whereEqualTo(FIELD_GROUP_CODE, groupCode)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.documents.isEmpty()) {
                return Result.failure(Exception("Group not found with code: $groupCode"))
            }
            
            val document = querySnapshot.documents[0]
            val group = Group(
                groupId = document.id,
                groupName = document.getString("groupName") ?: "",
                groupDescription = document.getString("groupDescription") ?: "",
                creatorId = document.getString("creatorId") ?: "",
                creatorName = document.getString("creatorName") ?: "",
                groupCode = document.getString("groupCode") ?: "",
                isPublic = document.getBoolean("isPublic") ?: false,
                createdAt = document.getLong("createdAt") ?: 0L,
                memberCount = document.getLong("memberCount")?.toInt() ?: 1,
                maxMembers = document.getLong("maxMembers")?.toInt() ?: 50,
                groupIcon = document.getString("groupIcon"),
                synced = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get public groups from Firestore
     * @param limit Maximum number of groups to fetch
     * @return Result with list of public groups
     */
    suspend fun getPublicGroups(limit: Int = 50): Result<List<Group>> {
        return try {
            if (!isNetworkAvailable()) {
                android.util.Log.e("GroupFirebaseService", "No internet connection for getPublicGroups")
                return Result.failure(Exception("No internet connection"))
            }
            
            android.util.Log.d("GroupFirebaseService", "Fetching public groups from Firestore...")
            
            // Composite index required - deployed via deploy_firestore_index.sh
            val querySnapshot = firestore.collection(COLLECTION_GROUPS)
                .whereEqualTo(FIELD_IS_PUBLIC, true)
                .orderBy(FIELD_MEMBER_COUNT, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            android.util.Log.d("GroupFirebaseService", "Found ${querySnapshot.documents.size} public groups")
            
            val groups = querySnapshot.documents.mapNotNull { document ->
                try {
                    android.util.Log.d("GroupFirebaseService", "Group: ${document.getString("groupName")} - isPublic: ${document.getBoolean("isPublic")}")
                    Group(
                        groupId = document.id,
                        groupName = document.getString("groupName") ?: "",
                        groupDescription = document.getString("groupDescription") ?: "",
                        creatorId = document.getString("creatorId") ?: "",
                        creatorName = document.getString("creatorName") ?: "",
                        groupCode = document.getString("groupCode") ?: "",
                        isPublic = document.getBoolean("isPublic") ?: false,
                        createdAt = document.getLong("createdAt") ?: 0L,
                        memberCount = document.getLong("memberCount")?.toInt() ?: 1,
                        maxMembers = document.getLong("maxMembers")?.toInt() ?: 50,
                        groupIcon = document.getString("groupIcon"),
                        synced = true,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("GroupFirebaseService", "Error parsing group document: ${e.message}")
                    null
                }
            }
            
            android.util.Log.d("GroupFirebaseService", "Successfully parsed ${groups.size} groups")
            Result.success(groups)
        } catch (e: Exception) {
            android.util.Log.e("GroupFirebaseService", "Error fetching public groups: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download all members of a group
     * @param groupId ID of the group
     * @return Result with list of members
     */
    suspend fun downloadGroupMembers(groupId: String): Result<List<GroupMember>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val querySnapshot = firestore.collection(COLLECTION_GROUPS)
                .document(groupId)
                .collection(COLLECTION_MEMBERS)
                .get()
                .await()
            
            val members = querySnapshot.documents.mapNotNull { document ->
                try {
                    GroupMember(
                        groupId = groupId,
                        memberId = document.id,
                        memberName = document.getString("memberName") ?: "",
                        role = MemberRole.valueOf(document.getString("role") ?: "MEMBER"),
                        joinedAt = document.getLong("joinedAt") ?: 0L,
                        totalScore = document.getLong("totalScore")?.toInt() ?: 0,
                        gamesPlayed = document.getLong("gamesPlayed")?.toInt() ?: 0,
                        challengesWon = document.getLong("challengesWon")?.toInt() ?: 0,
                        challengesLost = document.getLong("challengesLost")?.toInt() ?: 0,
                        lastActiveAt = document.getLong("lastActiveAt") ?: 0L,
                        isActive = document.getBoolean("isActive") ?: true,
                        synced = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download all group memberships for a specific user
     * Used to sync which groups the user is still a member of
     * @param userId The member ID to search for
     * @return Result with list of memberships (just groupId and memberId)
     */
    suspend fun downloadMembershipsByUser(userId: String): Result<List<GroupMember>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            android.util.Log.d("GroupFirebaseService", "Querying memberships for user: $userId")
            
            // Query all groups
            val groupsSnapshot = firestore.collection(COLLECTION_GROUPS)
                .get()
                .await()
            
            val memberships = mutableListOf<GroupMember>()
            
            // For each group, check if user is a member
            for (groupDoc in groupsSnapshot.documents) {
                val groupId = groupDoc.id
                val memberDoc = firestore.collection(COLLECTION_GROUPS)
                    .document(groupId)
                    .collection(COLLECTION_MEMBERS)
                    .document(userId)
                    .get()
                    .await()
                
                if (memberDoc.exists()) {
                    try {
                        val membership = GroupMember(
                            groupId = groupId,
                            memberId = userId,
                            memberName = memberDoc.getString("memberName") ?: "",
                            role = MemberRole.valueOf(memberDoc.getString("role") ?: "MEMBER"),
                            joinedAt = memberDoc.getLong("joinedAt") ?: 0L,
                            totalScore = memberDoc.getLong("totalScore")?.toInt() ?: 0,
                            gamesPlayed = memberDoc.getLong("gamesPlayed")?.toInt() ?: 0,
                            challengesWon = memberDoc.getLong("challengesWon")?.toInt() ?: 0,
                            challengesLost = memberDoc.getLong("challengesLost")?.toInt() ?: 0,
                            lastActiveAt = memberDoc.getLong("lastActiveAt") ?: 0L,
                            isActive = memberDoc.getBoolean("isActive") ?: true,
                            synced = true
                        )
                        memberships.add(membership)
                    } catch (e: Exception) {
                        android.util.Log.w("GroupFirebaseService", "Failed to parse membership in group $groupId: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("GroupFirebaseService", "Found ${memberships.size} memberships for user $userId")
            Result.success(memberships)
        } catch (e: Exception) {
            android.util.Log.e("GroupFirebaseService", "Error fetching memberships for user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a group from Firestore
     * @param groupId ID of the group to delete
     * @return Result with success or error
     */
    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Delete all members first
            val membersSnapshot = firestore.collection(COLLECTION_GROUPS)
                .document(groupId)
                .collection(COLLECTION_MEMBERS)
                .get()
                .await()
            
            val batch = firestore.batch()
            for (memberDoc in membersSnapshot.documents) {
                batch.delete(memberDoc.reference)
            }
            batch.commit().await()
            
            // Delete the group
            firestore.collection(COLLECTION_GROUPS)
                .document(groupId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a member from Firestore
     * @param groupId ID of the group
     * @param memberId ID of the member to delete
     * @return Result with success or error
     */
    suspend fun deleteMember(groupId: String, memberId: String): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            firestore.collection(COLLECTION_GROUPS)
                .document(groupId)
                .collection(COLLECTION_MEMBERS)
                .document(memberId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Listen to real-time updates for a group
     * @param groupId ID of the group to listen to
     * @return Flow of Group updates
     */
    fun listenToGroup(groupId: String): Flow<Group?> = callbackFlow {
        if (!isNetworkAvailable()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection(COLLECTION_GROUPS)
            .document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val group = Group(
                            groupId = snapshot.id,
                            groupName = snapshot.getString("groupName") ?: "",
                            groupDescription = snapshot.getString("groupDescription") ?: "",
                            creatorId = snapshot.getString("creatorId") ?: "",
                            creatorName = snapshot.getString("creatorName") ?: "",
                            groupCode = snapshot.getString("groupCode") ?: "",
                            isPublic = snapshot.getBoolean("isPublic") ?: false,
                            createdAt = snapshot.getLong("createdAt") ?: 0L,
                            memberCount = snapshot.getLong("memberCount")?.toInt() ?: 1,
                            maxMembers = snapshot.getLong("maxMembers")?.toInt() ?: 50,
                            groupIcon = snapshot.getString("groupIcon"),
                            synced = true,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                        trySend(group)
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Listen to real-time updates for group members
     * @param groupId ID of the group
     * @return Flow of list of members
     */
    fun listenToGroupMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        if (!isNetworkAvailable()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(COLLECTION_MEMBERS)
            .orderBy(FIELD_TOTAL_SCORE, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val members = snapshot.documents.mapNotNull { document ->
                        try {
                            GroupMember(
                                groupId = groupId,
                                memberId = document.id,
                                memberName = document.getString("memberName") ?: "",
                                role = MemberRole.valueOf(document.getString("role") ?: "MEMBER"),
                                joinedAt = document.getLong("joinedAt") ?: 0L,
                                totalScore = document.getLong("totalScore")?.toInt() ?: 0,
                                gamesPlayed = document.getLong("gamesPlayed")?.toInt() ?: 0,
                                challengesWon = document.getLong("challengesWon")?.toInt() ?: 0,
                                challengesLost = document.getLong("challengesLost")?.toInt() ?: 0,
                                lastActiveAt = document.getLong("lastActiveAt") ?: 0L,
                                isActive = document.getBoolean("isActive") ?: true,
                                synced = true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(members)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
