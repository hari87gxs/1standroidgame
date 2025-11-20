package com.athreya.mathworkout.data.avatar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Avatar operations
 */
@Dao
interface AvatarDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvatar(avatar: Avatar)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvatars(avatars: List<Avatar>)
    
    @Update
    suspend fun updateAvatar(avatar: Avatar)
    
    @Query("SELECT * FROM avatars ORDER BY xpCost ASC")
    fun getAllAvatars(): Flow<List<Avatar>>
    
    @Query("SELECT * FROM avatars WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAvatars(): Flow<List<Avatar>>
    
    @Query("SELECT * FROM avatars WHERE isUnlocked = 0 ORDER BY xpCost ASC")
    fun getLockedAvatars(): Flow<List<Avatar>>
    
    @Query("SELECT * FROM avatars WHERE avatarId = :avatarId")
    suspend fun getAvatar(avatarId: String): Avatar?
    
    @Query("SELECT * FROM avatars WHERE category = :category ORDER BY xpCost ASC")
    fun getAvatarsByCategory(category: AvatarCategory): Flow<List<Avatar>>
    
    @Query("SELECT * FROM avatars WHERE rarity = :rarity ORDER BY xpCost ASC")
    fun getAvatarsByRarity(rarity: AvatarRarity): Flow<List<Avatar>>
    
    @Query("UPDATE avatars SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE avatarId = :avatarId")
    suspend fun unlockAvatar(avatarId: String, unlockedAt: Long)
    
    @Query("SELECT COUNT(*) FROM avatars WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM avatars")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM avatars")
    suspend fun deleteAll()
}
