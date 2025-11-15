package com.athreya.mathworkout.data.social

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase service for syncing challenges to Firestore
 * 
 * Firestore Structure:
 * - Collection: "challenges"
 *   - Document ID: challengeId
 *   - Fields: groupId, challengerId, challengerName, challengedId, challengedName, etc.
 */
class ChallengeFirebaseService(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    companion object {
        private const val COLLECTION_CHALLENGES = "challenges"
        private const val FIELD_GROUP_ID = "groupId"
        private const val FIELD_CHALLENGER_ID = "challengerId"
        private const val FIELD_CHALLENGED_ID = "challengedId"
        private const val FIELD_STATUS = "status"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_EXPIRES_AT = "expiresAt"
    }
    
    /**
     * Upload a challenge to Firestore
     * @param challenge Challenge to upload
     * @return Result with success or error
     */
    suspend fun uploadChallenge(challenge: Challenge): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val challengeData = mapOf(
                "challengeId" to challenge.challengeId,
                "groupId" to challenge.groupId,
                "challengerId" to challenge.challengerId,
                "challengerName" to challenge.challengerName,
                "challengedId" to challenge.challengedId,
                "challengedName" to challenge.challengedName,
                "gameMode" to challenge.gameMode.name,
                "difficulty" to challenge.difficulty.name,
                "questionCount" to challenge.questionCount,
                "challengerScore" to challenge.challengerScore,
                "challengerTime" to challenge.challengerTime,
                "challengerCompleted" to challenge.challengerCompleted,
                "challengerCompletedAt" to challenge.challengerCompletedAt,
                "challengedScore" to challenge.challengedScore,
                "challengedTime" to challenge.challengedTime,
                "challengedCompleted" to challenge.challengedCompleted,
                "challengedCompletedAt" to challenge.challengedCompletedAt,
                "status" to challenge.status.name,
                "createdAt" to challenge.createdAt,
                "expiresAt" to challenge.expiresAt,
                "winnerId" to challenge.winnerId,
                "winnerName" to challenge.winnerName,
                "lastUpdatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_CHALLENGES)
                .document(challenge.challengeId)
                .set(challengeData, SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download a challenge from Firestore by ID
     * @param challengeId ID of the challenge
     * @return Result with Challenge or error
     */
    suspend fun downloadChallenge(challengeId: String): Result<Challenge> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val document = firestore.collection(COLLECTION_CHALLENGES)
                .document(challengeId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Challenge not found"))
            }
            
            val challenge = Challenge(
                challengeId = document.getString("challengeId") ?: challengeId,
                groupId = document.getString("groupId") ?: "",
                challengerId = document.getString("challengerId") ?: "",
                challengerName = document.getString("challengerName") ?: "",
                challengedId = document.getString("challengedId"),
                challengedName = document.getString("challengedName"),
                gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION"),
                difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                challengerTime = document.getLong("challengerTime") ?: 0L,
                challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                challengerCompletedAt = document.getLong("challengerCompletedAt"),
                challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                challengedTime = document.getLong("challengedTime") ?: 0L,
                challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                challengedCompletedAt = document.getLong("challengedCompletedAt"),
                status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                createdAt = document.getLong("createdAt") ?: 0L,
                expiresAt = document.getLong("expiresAt") ?: 0L,
                winnerId = document.getString("winnerId"),
                winnerName = document.getString("winnerName"),
                synced = true,
                lastSyncedAt = System.currentTimeMillis()
            )
            
            Result.success(challenge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get challenges for a specific group
     * @param groupId ID of the group
     * @param limit Maximum number of challenges to fetch
     * @return Result with list of challenges
     */
    suspend fun getGroupChallenges(groupId: String, limit: Int = 100): Result<List<Challenge>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val querySnapshot = firestore.collection(COLLECTION_CHALLENGES)
                .whereEqualTo(FIELD_GROUP_ID, groupId)
                .orderBy(FIELD_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val challenges = querySnapshot.documents.mapNotNull { document ->
                try {
                    Challenge(
                        challengeId = document.id,
                        groupId = document.getString("groupId") ?: "",
                        challengerId = document.getString("challengerId") ?: "",
                        challengerName = document.getString("challengerName") ?: "",
                        challengedId = document.getString("challengedId"),
                        challengedName = document.getString("challengedName"),
                        gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION"),
                        difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                        questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                        challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                        challengerTime = document.getLong("challengerTime") ?: 0L,
                        challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                        challengerCompletedAt = document.getLong("challengerCompletedAt"),
                        challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                        challengedTime = document.getLong("challengedTime") ?: 0L,
                        challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                        challengedCompletedAt = document.getLong("challengedCompletedAt"),
                        status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                        createdAt = document.getLong("createdAt") ?: 0L,
                        expiresAt = document.getLong("expiresAt") ?: 0L,
                        winnerId = document.getString("winnerId"),
                        winnerName = document.getString("winnerName"),
                        synced = true,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(challenges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get pending challenges for a specific user
     * @param memberId Device ID of the member
     * @param limit Maximum number of challenges to fetch
     * @return Result with list of pending challenges
     */
    suspend fun getPendingChallenges(memberId: String, limit: Int = 50): Result<List<Challenge>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val querySnapshot = firestore.collection(COLLECTION_CHALLENGES)
                .whereEqualTo(FIELD_CHALLENGED_ID, memberId)
                .whereEqualTo(FIELD_STATUS, "PENDING")
                .orderBy(FIELD_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val challenges = querySnapshot.documents.mapNotNull { document ->
                try {
                    Challenge(
                        challengeId = document.id,
                        groupId = document.getString("groupId") ?: "",
                        challengerId = document.getString("challengerId") ?: "",
                        challengerName = document.getString("challengerName") ?: "",
                        challengedId = document.getString("challengedId"),
                        challengedName = document.getString("challengedName"),
                        gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION"),
                        difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                        questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                        challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                        challengerTime = document.getLong("challengerTime") ?: 0L,
                        challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                        challengerCompletedAt = document.getLong("challengerCompletedAt"),
                        challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                        challengedTime = document.getLong("challengedTime") ?: 0L,
                        challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                        challengedCompletedAt = document.getLong("challengedCompletedAt"),
                        status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                        createdAt = document.getLong("createdAt") ?: 0L,
                        expiresAt = document.getLong("expiresAt") ?: 0L,
                        winnerId = document.getString("winnerId"),
                        winnerName = document.getString("winnerName"),
                        synced = true,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(challenges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a challenge from Firestore
     * @param challengeId ID of the challenge to delete
     * @return Result with success or error
     */
    suspend fun deleteChallenge(challengeId: String): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            firestore.collection(COLLECTION_CHALLENGES)
                .document(challengeId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Listen to real-time updates for a challenge
     * @param challengeId ID of the challenge to listen to
     * @return Flow of Challenge updates
     */
    fun listenToChallenge(challengeId: String): Flow<Challenge?> = callbackFlow {
        if (!isNetworkAvailable()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection(COLLECTION_CHALLENGES)
            .document(challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val challenge = Challenge(
                            challengeId = snapshot.id,
                            groupId = snapshot.getString("groupId") ?: "",
                            challengerId = snapshot.getString("challengerId") ?: "",
                            challengerName = snapshot.getString("challengerName") ?: "",
                            challengedId = snapshot.getString("challengedId"),
                            challengedName = snapshot.getString("challengedName"),
                            gameMode = GameMode.valueOf(snapshot.getString("gameMode") ?: "ADDITION"),
                            difficulty = Difficulty.valueOf(snapshot.getString("difficulty") ?: "EASY"),
                            questionCount = snapshot.getLong("questionCount")?.toInt() ?: 10,
                            challengerScore = snapshot.getLong("challengerScore")?.toInt() ?: 0,
                            challengerTime = snapshot.getLong("challengerTime") ?: 0L,
                            challengerCompleted = snapshot.getBoolean("challengerCompleted") ?: false,
                            challengerCompletedAt = snapshot.getLong("challengerCompletedAt"),
                            challengedScore = snapshot.getLong("challengedScore")?.toInt() ?: 0,
                            challengedTime = snapshot.getLong("challengedTime") ?: 0L,
                            challengedCompleted = snapshot.getBoolean("challengedCompleted") ?: false,
                            challengedCompletedAt = snapshot.getLong("challengedCompletedAt"),
                            status = ChallengeStatus.valueOf(snapshot.getString("status") ?: "PENDING"),
                            createdAt = snapshot.getLong("createdAt") ?: 0L,
                            expiresAt = snapshot.getLong("expiresAt") ?: 0L,
                            winnerId = snapshot.getString("winnerId"),
                            winnerName = snapshot.getString("winnerName"),
                            synced = true,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                        trySend(challenge)
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
     * Listen to real-time updates for pending challenges for a user
     * @param memberId Device ID of the member
     * @return Flow of list of pending challenges
     */
    fun listenToPendingChallenges(memberId: String): Flow<List<Challenge>> = callbackFlow {
        if (!isNetworkAvailable()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection(COLLECTION_CHALLENGES)
            .whereEqualTo(FIELD_CHALLENGED_ID, memberId)
            .whereEqualTo(FIELD_STATUS, "PENDING")
            .orderBy(FIELD_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val challenges = snapshot.documents.mapNotNull { document ->
                        try {
                            Challenge(
                                challengeId = document.id,
                                groupId = document.getString("groupId") ?: "",
                                challengerId = document.getString("challengerId") ?: "",
                                challengerName = document.getString("challengerName") ?: "",
                                challengedId = document.getString("challengedId"),
                                challengedName = document.getString("challengedName"),
                                gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION"),
                                difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                                questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                                challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                                challengerTime = document.getLong("challengerTime") ?: 0L,
                                challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                                challengerCompletedAt = document.getLong("challengerCompletedAt"),
                                challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                                challengedTime = document.getLong("challengedTime") ?: 0L,
                                challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                                challengedCompletedAt = document.getLong("challengedCompletedAt"),
                                status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                                createdAt = document.getLong("createdAt") ?: 0L,
                                expiresAt = document.getLong("expiresAt") ?: 0L,
                                winnerId = document.getString("winnerId"),
                                winnerName = document.getString("winnerName"),
                                synced = true,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(challenges)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Listen to real-time updates for group challenges
     * @param groupId ID of the group
     * @return Flow of list of group challenges
     */
    fun listenToGroupChallenges(groupId: String): Flow<List<Challenge>> = callbackFlow {
        if (!isNetworkAvailable()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection(COLLECTION_CHALLENGES)
            .whereEqualTo(FIELD_GROUP_ID, groupId)
            .orderBy(FIELD_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val challenges = snapshot.documents.mapNotNull { document ->
                        try {
                            Challenge(
                                challengeId = document.id,
                                groupId = document.getString("groupId") ?: "",
                                challengerId = document.getString("challengerId") ?: "",
                                challengerName = document.getString("challengerName") ?: "",
                                challengedId = document.getString("challengedId"),
                                challengedName = document.getString("challengedName"),
                                gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION"),
                                difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                                questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                                challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                                challengerTime = document.getLong("challengerTime") ?: 0L,
                                challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                                challengerCompletedAt = document.getLong("challengerCompletedAt"),
                                challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                                challengedTime = document.getLong("challengedTime") ?: 0L,
                                challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                                challengedCompletedAt = document.getLong("challengedCompletedAt"),
                                status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                                createdAt = document.getLong("createdAt") ?: 0L,
                                expiresAt = document.getLong("expiresAt") ?: 0L,
                                winnerId = document.getString("winnerId"),
                                winnerName = document.getString("winnerName"),
                                synced = true,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(challenges)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Download all challenges for a specific member (challenger or challenged)
     * @param memberId Device ID of the member
     * @return Result with list of challenges or error
     */
    suspend fun downloadMemberChallenges(memberId: String): Result<List<Challenge>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            android.util.Log.d("ChallengeFirebaseService", "Downloading challenges for member: $memberId")
            
            // Query challenges where user is either challenger or challenged
            val challengerQuery = firestore.collection(COLLECTION_CHALLENGES)
                .whereEqualTo(FIELD_CHALLENGER_ID, memberId)
                .get()
                .await()
            
            val challengedQuery = firestore.collection(COLLECTION_CHALLENGES)
                .whereEqualTo(FIELD_CHALLENGED_ID, memberId)
                .get()
                .await()
            
            val allDocuments = challengerQuery.documents + challengedQuery.documents
            android.util.Log.d("ChallengeFirebaseService", "Found ${allDocuments.size} challenge documents")
            
            val challenges = allDocuments.mapNotNull { document ->
                try {
                    Challenge(
                        challengeId = document.id,
                        groupId = document.getString("groupId") ?: "",
                        challengerId = document.getString("challengerId") ?: "",
                        challengerName = document.getString("challengerName") ?: "",
                        challengedId = document.getString("challengedId"),
                        challengedName = document.getString("challengedName") ?: "",
                        gameMode = GameMode.valueOf(document.getString("gameMode") ?: "ADDITION_SUBTRACTION"),
                        difficulty = Difficulty.valueOf(document.getString("difficulty") ?: "EASY"),
                        questionCount = document.getLong("questionCount")?.toInt() ?: 10,
                        challengerScore = document.getLong("challengerScore")?.toInt() ?: 0,
                        challengerTime = document.getLong("challengerTime") ?: 0L,
                        challengerCompleted = document.getBoolean("challengerCompleted") ?: false,
                        challengerCompletedAt = document.getLong("challengerCompletedAt"),
                        challengedScore = document.getLong("challengedScore")?.toInt() ?: 0,
                        challengedTime = document.getLong("challengedTime") ?: 0L,
                        challengedCompleted = document.getBoolean("challengedCompleted") ?: false,
                        challengedCompletedAt = document.getLong("challengedCompletedAt"),
                        status = ChallengeStatus.valueOf(document.getString("status") ?: "PENDING"),
                        createdAt = document.getLong("createdAt") ?: 0L,
                        expiresAt = document.getLong("expiresAt") ?: 0L,
                        winnerId = document.getString("winnerId"),
                        winnerName = document.getString("winnerName"),
                        synced = true,
                        lastSyncedAt = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ChallengeFirebaseService", "Error parsing challenge document: ${document.id}", e)
                    null
                }
            }
            
            android.util.Log.d("ChallengeFirebaseService", "Successfully parsed ${challenges.size} challenges")
            Result.success(challenges)
        } catch (e: Exception) {
            android.util.Log.e("ChallengeFirebaseService", "Error downloading member challenges", e)
            Result.failure(e)
        }
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
