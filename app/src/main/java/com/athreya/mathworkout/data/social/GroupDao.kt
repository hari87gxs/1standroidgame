package com.athreya.mathworkout.data.social

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Group operations
 */
@Dao
interface GroupDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)
    
    @Update
    suspend fun updateGroup(group: Group)
    
    @Delete
    suspend fun deleteGroup(group: Group)
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: String): Group?
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    fun getGroupByIdFlow(groupId: String): Flow<Group?>
    
    @Query("SELECT * FROM groups WHERE groupCode = :groupCode")
    suspend fun getGroupByCode(groupCode: String): Group?
    
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<Group>>
    
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    suspend fun getAllGroupsSync(): List<Group>
    
    @Query("SELECT * FROM groups WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getGroupsCreatedBy(creatorId: String): Flow<List<Group>>
    
    @Query("SELECT * FROM groups WHERE isPublic = 1 ORDER BY memberCount DESC, createdAt DESC")
    fun getPublicGroups(): Flow<List<Group>>
    
    @Query("UPDATE groups SET memberCount = :count WHERE groupId = :groupId")
    suspend fun updateMemberCount(groupId: String, count: Int)
    
    @Query("UPDATE groups SET synced = :synced, lastSyncedAt = :timestamp WHERE groupId = :groupId")
    suspend fun updateSyncStatus(groupId: String, synced: Boolean, timestamp: Long)
    
    @Query("SELECT * FROM groups WHERE synced = 0")
    suspend fun getUnsyncedGroups(): List<Group>
    
    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroupById(groupId: String)
}
