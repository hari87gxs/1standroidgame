package com.athreya.mathworkout.data.repository

import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.avatar.Avatar
import com.athreya.mathworkout.data.avatar.AvatarCategory
import com.athreya.mathworkout.data.avatar.AvatarCollection
import com.athreya.mathworkout.data.avatar.AvatarDao
import com.athreya.mathworkout.data.avatar.AvatarRarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for managing avatars
 * Handles avatar unlocking, selection, and XP spending
 */
class AvatarRepository(
    private val avatarDao: AvatarDao,
    private val userPreferences: UserPreferencesManager
) {
    
    /**
     * Initialize avatar collection if needed
     */
    suspend fun initializeAvatars() {
        val totalCount = avatarDao.getTotalCount()
        if (totalCount == 0) {
            // First time - insert all avatars
            avatarDao.insertAvatars(AvatarCollection.getAllAvatars())
            android.util.Log.d("AvatarRepository", "Initialized ${AvatarCollection.getAllAvatars().size} avatars")
            
            // Unlock the first COMMON avatar (Pythagoras) for free as a starter
            avatarDao.unlockAvatar("pythagoras", System.currentTimeMillis())
            android.util.Log.d("AvatarRepository", "Unlocked starter avatar: Pythagoras")
        }
    }
    
    /**
     * Get all avatars
     */
    fun getAllAvatars(): Flow<List<Avatar>> = avatarDao.getAllAvatars()
    
    /**
     * Get unlocked avatars
     */
    fun getUnlockedAvatars(): Flow<List<Avatar>> = avatarDao.getUnlockedAvatars()
    
    /**
     * Get locked avatars
     */
    fun getLockedAvatars(): Flow<List<Avatar>> = avatarDao.getLockedAvatars()
    
    /**
     * Get avatars by category
     */
    fun getAvatarsByCategory(category: AvatarCategory): Flow<List<Avatar>> =
        avatarDao.getAvatarsByCategory(category)
    
    /**
     * Get avatars by rarity
     */
    fun getAvatarsByRarity(rarity: AvatarRarity): Flow<List<Avatar>> =
        avatarDao.getAvatarsByRarity(rarity)
    
    /**
     * Get currently selected avatar
     */
    fun getSelectedAvatar(): String {
        return userPreferences.getSelectedAvatar()
    }
    
    /**
     * Set selected avatar
     */
    suspend fun selectAvatar(avatarId: String): Result<Unit> {
        return try {
            val avatar = avatarDao.getAvatar(avatarId)
            if (avatar == null) {
                return Result.failure(Exception("Avatar not found"))
            }
            
            if (!avatar.isUnlocked) {
                return Result.failure(Exception("Avatar is locked. Unlock it first!"))
            }
            
            userPreferences.setSelectedAvatar(avatarId)
            android.util.Log.d("AvatarRepository", "Selected avatar: ${avatar.name} (${avatar.era})")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AvatarRepository", "Error selecting avatar", e)
            Result.failure(e)
        }
    }
    
    /**
     * Unlock avatar using XP points
     */
    suspend fun unlockAvatar(avatarId: String): Result<Avatar> {
        return try {
            val avatar = avatarDao.getAvatar(avatarId)
            if (avatar == null) {
                return Result.failure(Exception("Avatar not found"))
            }
            
            if (avatar.isUnlocked) {
                return Result.failure(Exception("Avatar already unlocked!"))
            }
            
            // Check if user has enough XP
            val currentXP = userPreferences.getTotalXP()
            if (currentXP < avatar.xpCost) {
                return Result.failure(Exception("Not enough XP! Need ${avatar.xpCost}, have $currentXP"))
            }
            
            // Deduct XP
            val newXP = currentXP - avatar.xpCost
            userPreferences.setTotalXP(newXP)
            
            // Unlock avatar
            avatarDao.unlockAvatar(avatarId, System.currentTimeMillis())
            
            android.util.Log.d("AvatarRepository", "Unlocked ${avatar.name}! Spent ${avatar.xpCost} XP. Remaining: $newXP")
            
            // Return updated avatar
            val unlockedAvatar = avatarDao.getAvatar(avatarId)!!
            Result.success(unlockedAvatar)
        } catch (e: Exception) {
            android.util.Log.e("AvatarRepository", "Error unlocking avatar", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get unlock statistics
     */
    suspend fun getUnlockStats(): UnlockStats {
        val allAvatars = avatarDao.getAllAvatars().first()
        val unlockedCount = allAvatars.count { it.isUnlocked }
        val totalCount = allAvatars.size
        val currentXP = userPreferences.getTotalXP()
        
        return UnlockStats(
            unlockedCount = unlockedCount,
            totalCount = totalCount,
            currentXP = currentXP
        )
    }
    
    /**
     * Get next affordable avatar
     */
    suspend fun getNextAffordableAvatar(): Avatar? {
        val currentXP = userPreferences.getTotalXP()
        val lockedAvatars = avatarDao.getLockedAvatars().first()
        return lockedAvatars
            .filter { it.xpCost <= currentXP }
            .minByOrNull { it.xpCost }
    }
}

/**
 * Unlock statistics data class
 */
data class UnlockStats(
    val unlockedCount: Int,
    val totalCount: Int,
    val currentXP: Int
) {
    val progress: Float
        get() = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
    
    val percentage: Int
        get() = (progress * 100).toInt()
}
