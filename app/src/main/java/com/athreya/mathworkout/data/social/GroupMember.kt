package com.athreya.mathworkout.data.social

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * GroupMember entity for tracking members in groups
 * Links players to their groups
 */
@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "memberId"]
)
data class GroupMember(
    val groupId: String, // Reference to Group
    val memberId: String, // Device ID
    val memberName: String,
    
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    
    // Member stats within this group
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val challengesWon: Int = 0,
    val challengesLost: Int = 0,
    
    // Activity tracking
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    
    // Sync status
    val synced: Boolean = false
)

/**
 * Member roles within a group
 */
enum class MemberRole {
    CREATOR,  // Group creator (can delete group, remove members)
    ADMIN,    // Can invite members, manage challenges
    MEMBER    // Regular member (can participate in challenges)
}
