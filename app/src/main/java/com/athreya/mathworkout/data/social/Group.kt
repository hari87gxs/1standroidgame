package com.athreya.mathworkout.data.social

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Group entity for social features
 * Represents a group of players who can compete and challenge each other
 */
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey
    val groupId: String, // Unique group ID (Firebase document ID)
    
    val groupName: String,
    val groupDescription: String = "",
    val creatorId: String, // Device ID of creator
    val creatorName: String,
    
    val groupCode: String, // 6-digit code for joining
    val isPublic: Boolean = false, // Public groups can be discovered
    
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 1,
    val maxMembers: Int = 50,
    
    // Group avatar/icon (optional)
    val groupIcon: String? = null,
    
    // Sync status
    val synced: Boolean = false,
    val lastSyncedAt: Long = 0L
) {
    companion object {
        /**
         * Generate a random 6-digit group code
         */
        fun generateGroupCode(): String {
            return (100000..999999).random().toString()
        }
    }
}
