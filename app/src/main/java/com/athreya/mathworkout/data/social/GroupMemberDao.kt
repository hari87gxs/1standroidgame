package com.athreya.mathworkout.data.social

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for GroupMember operations
 */
@Dao
interface GroupMemberDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMember)
    
    @Update
    suspend fun updateMember(member: GroupMember)
    
    @Delete
    suspend fun deleteMember(member: GroupMember)
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun getMember(groupId: String, memberId: String): GroupMember?
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND memberId = :memberId")
    fun getMemberFlow(groupId: String, memberId: String): Flow<GroupMember?>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY totalScore DESC, joinedAt ASC")
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY totalScore DESC LIMIT :limit")
    fun getTopMembers(groupId: String, limit: Int): Flow<List<GroupMember>>
    
    @Query("SELECT * FROM group_members WHERE memberId = :memberId")
    fun getMemberGroups(memberId: String): Flow<List<GroupMember>>
    
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND isActive = 1")
    suspend fun getActiveMemberCount(groupId: String): Int
    
    @Query("UPDATE group_members SET totalScore = totalScore + :score, gamesPlayed = gamesPlayed + 1, lastActiveAt = :timestamp WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun updateMemberStats(groupId: String, memberId: String, score: Int, timestamp: Long)
    
    @Query("UPDATE group_members SET challengesWon = challengesWon + 1 WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun incrementChallengesWon(groupId: String, memberId: String)
    
    @Query("UPDATE group_members SET challengesLost = challengesLost + 1 WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun incrementChallengesLost(groupId: String, memberId: String)
    
    @Query("UPDATE group_members SET synced = :synced WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun updateSyncStatus(groupId: String, memberId: String, synced: Boolean)
    
    @Query("SELECT * FROM group_members WHERE synced = 0")
    suspend fun getUnsyncedMembers(): List<GroupMember>
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteAllGroupMembers(groupId: String)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND memberId = :memberId")
    suspend fun removeMember(groupId: String, memberId: String)
}
