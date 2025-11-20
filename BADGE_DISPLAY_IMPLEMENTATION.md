# Badge Display Implementation

## Overview
Implemented badge display across multiple screens to showcase user achievements throughout the app.

## Changes Made

### 1. Home Screen Welcome Section
**File**: `app/src/main/java/com/athreya/mathworkout/ui/screens/HomeScreen.kt`

- Added badge row display below the player's welcome message
- Shows up to 5 unlocked badges with compact badge indicators
- Badges appear only for registered users
- Positioned below the player name and rank icon

**Visual Layout**:
```
┌─────────────────────────────────────┐
│  👋 PlayerName 🏆                   │
│  🎯 ⚡ 💎                            │  <- Badge Row
└─────────────────────────────────────┘
```

### 2. HomeViewModel Enhancement
**File**: `app/src/main/java/com/athreya/mathworkout/viewmodel/HomeViewModel.kt`

- Added `BadgeManager` integration
- Added `unlockedBadges: List<Badge>` to `HomeUiState`
- Added `loadBadges()` function to fetch unlocked badges on initialization
- Badges are loaded when the app starts and displayed on home screen

### 3. Global Leaderboard
**File**: `app/src/main/java/com/athreya/mathworkout/ui/components/GlobalLeaderboardScreen.kt`

- Modified `LeaderboardItem` composable to display badges next to player names
- Shows top 3 badges for each player in the leaderboard
- Badges appear inline with the player name

**Visual Layout**:
```
┌──────────────────────────────────────┐
│ #1 │ PlayerName 🎯 ⚡ 💎  │ 500 pts │
│    │ ADDITION • EASY        │ 2:30   │
└──────────────────────────────────────┘
```

### 4. Group Leaderboard
**File**: `app/src/main/java/com/athreya/mathworkout/ui/screens/GroupDetailScreen.kt`

- Modified `LeaderboardCard` composable to show badges for current user
- Displays up to 3 badges below the player name
- Only shows badges for the current user (not other group members)
- Helps identify your own entry in group rankings

**Visual Layout**:
```
┌──────────────────────────────────────┐
│ �� │ PlayerName (You)        │ Admin│
│    │ 🎯 ⚡ ��                 │      │
│    │ 15 games • Avg: 450     │      │
└──────────────────────────────────────┘
```

### 5. Challenge Screens
**File**: `app/src/main/java/com/athreya/mathworkout/ui/screens/ChallengesScreen.kt`

- Modified `PendingChallengeCard` to show challenger's badges
- Displays up to 3 badges below the challenger's name
- Shows the skill level of the person challenging you
- Helps you decide whether to accept the challenge

**Visual Layout**:
```
┌─────────────────────────────────────┐
│ ChallengeName challenges you!       │
│ 🎯 ⚡ 💎                             │  <- Challenger badges
│ Game: ADDITION                      │
│ 10 questions • EASY                 │
│ [Decline]  [Accept ⚔️]              │
└─────────────────────────────────────┘
```

## Badge Display Components Used

### BadgeRow
- Displays badges horizontally in a compact row
- Parameters:
  - `badges`: List of Badge objects to display
  - `maxBadges`: Maximum number of badges to show (default 5)
  - Shows "+X" indicator if more badges exist
- Size: 24dp circular indicators with emoji

### BadgeIndicator
- Individual badge display (used by BadgeRow)
- Shows emoji with colored circular background
- Border color matches badge rarity
- Size: 24dp (customizable)

## Badge Loading Strategy

Currently, badges are loaded from the current user's profile:
- **Home Screen**: Loads user's own badges
- **Global Leaderboard**: Loads user's own badges for all entries (temporary)
- **Group Leaderboard**: Loads user's own badges, only shown for current user
- **Challenges**: Loads user's own badges to represent challenger

**Future Enhancement**: 
In a full implementation with backend support, each player's badges would be stored with their profile and fetched from the server for display in leaderboards and challenges.

## Visual Benefits

1. **Recognition**: Players see their achievements prominently displayed
2. **Motivation**: Seeing badges in challenges motivates players to compete
3. **Social Proof**: Badges on leaderboards show skill and dedication
4. **Personalization**: Home screen feels more personalized with badge display
5. **Competitive Edge**: Challenger badges set expectations in challenges

## Technical Notes

- All badge displays use existing `BadgeDisplay.kt` components
- No new database or API changes required
- Badges are loaded using `BadgeManager`
- Badge data comes from `UserPreferencesManager`
- Consistent 24dp size across all displays
- Maximum 3-5 badges shown to avoid clutter

## Testing Checklist

- [x] Badges appear on home screen for registered users
- [ ] Badges appear on global leaderboard entries
- [ ] Badges appear for current user in group leaderboard
- [ ] Badges appear when receiving a challenge
- [ ] Badge count indicator (+X) works correctly
- [ ] Layout remains responsive with/without badges
- [ ] No crashes when user has no badges

## Files Modified

1. `viewmodel/HomeViewModel.kt` - Added badge loading
2. `ui/screens/HomeScreen.kt` - Display badges in welcome section
3. `ui/components/GlobalLeaderboardScreen.kt` - Display badges in leaderboard items
4. `ui/screens/GroupDetailScreen.kt` - Display badges in group leaderboard
5. `ui/screens/ChallengesScreen.kt` - Display challenger badges

## Next Steps

For production app:
1. Store badge data with user profiles in backend
2. Fetch badge data when loading leaderboards/challenges
3. Add badge click handler to show badge details
4. Consider badge filtering (show only highest rarity)
5. Add animations when badges are unlocked
