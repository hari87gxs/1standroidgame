# Group Member System - How It Works

## Current Implementation

### Two Ways to Add Members to a Group:

#### 1. **Join by Group Code (RECOMMENDED)** ✅
- **How**: User clicks "Join Group" on Groups screen, enters 6-digit code
- **What happens**: 
  - User's device ID becomes their member ID
  - User's registered player name is used
  - Scores automatically sync when they play games
  - User can see the group in their "My Groups" list
  - User can participate in challenges
  - Real-time updates work properly

**This is the BEST way** because it creates a fully functional membership linked to the actual user's device.

#### 2. **Add Member Manually (PLACEHOLDER)** ⚠️
- **How**: Group creator/admin clicks "Add Member" button, enters a name
- **What happens**:
  - A random member ID is generated (not linked to any device)
  - Member appears in the leaderboard with score 0
  - This is a "ghost" member - just a placeholder
  - They CANNOT log in or see the group
  - Scores do NOT automatically sync
  - Intended for manual tracking only

**Use this only for:**
- Tracking offline players manually
- Adding someone who doesn't have the app yet
- Placeholder for someone you'll invite later

## How Scores Sync

### For Real Members (Joined by Code):
```kotlin
// After each game, in GameViewModel:
updateMemberStatsAfterGame(groupId, score)
  ↓
Repository checks if player is in any groups
  ↓
Updates their totalScore, gamesPlayed in GroupMember table
  ↓
Syncs to Firebase automatically
  ↓
Leaderboard updates in real-time for all group members
```

### For Placeholder Members:
- Scores stay at 0 unless manually updated
- No automatic sync happens
- Group admin would need to manually update their score (feature not yet implemented)

## Recommended Flow

1. **Creator creates group** (gets 6-digit code)
2. **Creator shares code** with friends (via text, email, etc.)
3. **Friends join using code** on their devices
4. **Everyone plays games** → scores automatically update
5. **Leaderboard shows real-time rankings**

## Future Improvements

### Option A: User Lookup System
Add ability to search for users by:
- Player name
- Email address
- Device ID
Then send them an invitation

### Option B: Invitation Links
Generate deep links like:
`mathworkout://join-group?code=311170`
That can be shared and clicked to auto-join

### Option C: QR Code
Display QR code for the group code that friends can scan

### Option D: Manual Score Update
Allow admins to edit placeholder member scores manually

## Current Limitations

- No user authentication system (uses device ID only)
- No way to search for other users
- No push notifications for group invites
- No email/SMS invitations
- Placeholder members are static (no score updates)

## Testing the System

1. Create a group on Device 1
2. Note the 6-digit code
3. Use the code to join from Device 2 (or same device with different device ID)
4. Play a game on Device 2
5. Check the group leaderboard - score should update automatically
6. Try adding a placeholder member - they'll appear but scores won't update

## Summary

**For a fully functional group experience, always use "Join by Code".**

The "Add Member" button creates placeholder entries only - useful for tracking but not for real gameplay integration.
