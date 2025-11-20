# Challenge Notification System Implementation

## Overview
Users now receive push notifications when they receive a new challenge from another player.

## What Was Implemented

### 1. **Dependencies Added**
- `firebase-messaging-ktx` - Firebase Cloud Messaging library for push notifications

### 2. **Permissions**
- Added `POST_NOTIFICATIONS` permission in AndroidManifest.xml (required for Android 13+)
- Permission is automatically requested when the app starts

### 3. **Firebase Messaging Service**
**File:** `services/FirebaseMessagingService.kt`

Handles incoming push notifications with three types:
- `new_challenge` - When someone challenges you
- `challenge_accepted` - When someone accepts your challenge
- `challenge_completed` - When someone completes a challenge

### 4. **Notification Helper**
**File:** `utils/NotificationHelper.kt`

Utility functions for:
- Requesting notification permission
- Checking permission status
- Getting FCM token
- Subscribing/unsubscribing to topics

### 5. **Notification Strings**
Added to `strings.xml`:
- Channel names and IDs
- Notification titles and messages

## How It Works

### Client Side (Android App)
1. **On App Start:**
   - App requests notification permission (Android 13+)
   - Gets FCM token from Firebase
   - Token is logged for testing

2. **When Notification Arrives:**
   - `FirebaseMessagingService` receives the message
   - Creates a notification with title and body
   - Shows notification in status bar
   - Clicking notification opens the Challenges screen

### Server Side (Firebase Functions - TODO)
To send notifications when a challenge is created, you need to set up Firebase Cloud Functions:

```javascript
// Example Firebase Cloud Function
exports.sendChallengeNotification = functions.firestore
    .document('challenges/{challengeId}')
    .onCreate(async (snapshot, context) => {
        const challenge = snapshot.data();
        const receiverId = challenge.receiverId;
        
        // Get receiver's FCM token from Firestore
        const userDoc = await admin.firestore()
            .collection('users')
            .doc(receiverId)
            .get();
        
        const fcmToken = userDoc.data().fcmToken;
        
        if (fcmToken) {
            const message = {
                token: fcmToken,
                notification: {
                    title: 'New Challenge!',
                    body: `${challenge.senderName} has challenged you!`
                },
                data: {
                    type: 'new_challenge',
                    challenge_id: context.params.challengeId,
                    challenger_name: challenge.senderName
                }
            };
            
            await admin.messaging().send(message);
        }
    });
```

## Testing Notifications

### 1. **Get Your FCM Token**
- Run the app
- Check logcat for: `FCM Token: <your_token>`
- Copy this token

### 2. **Test with Firebase Console**
1. Go to Firebase Console → Cloud Messaging
2. Click "Send your first message"
3. Enter notification title and body
4. Click "Send test message"
5. Paste your FCM token
6. Click "Test"

### 3. **Test with curl**
```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "DEVICE_FCM_TOKEN",
    "notification": {
      "title": "New Challenge!",
      "body": "John has challenged you to a math duel!"
    },
    "data": {
      "type": "new_challenge",
      "challenge_id": "test123",
      "challenger_name": "John"
    }
  }'
```

## Next Steps

### Required for Production:
1. **Store FCM Tokens in Firestore**
   - When user logs in, save their FCM token to Firestore
   - Update token in `onNewToken()` callback

2. **Set Up Firebase Cloud Functions**
   - Create function to send notifications when challenges are created
   - Handle challenge accepted/completed notifications

3. **Update ChallengeRepository**
   - Send FCM token to Firestore when creating/updating user profile
   - Trigger notifications via Cloud Functions

### Example: Storing FCM Token in Firestore

```kotlin
// In your user profile setup
NotificationHelper.getFCMToken { token ->
    token?.let {
        // Store in Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", it)
    }
}
```

## Notification Payload Structure

### For New Challenges:
```json
{
  "notification": {
    "title": "New Challenge!",
    "body": "John has challenged you!"
  },
  "data": {
    "type": "new_challenge",
    "challenge_id": "abc123",
    "challenger_name": "John"
  }
}
```

### For Challenge Accepted:
```json
{
  "data": {
    "type": "challenge_accepted",
    "accepter_name": "Jane",
    "challenge_id": "abc123"
  }
}
```

### For Challenge Completed:
```json
{
  "data": {
    "type": "challenge_completed",
    "completed_by": "Jane",
    "challenge_id": "abc123"
  }
}
```

## Troubleshooting

### Notifications Not Appearing:
1. Check notification permission is granted
2. Verify FCM token is being retrieved
3. Check logcat for FCM messages
4. Ensure Firebase project is correctly configured
5. Verify google-services.json is up to date

### Token Not Generating:
1. Check internet connection
2. Verify Firebase dependencies in build.gradle
3. Ensure google-services.json is in app/ folder
4. Clean and rebuild project

## Files Modified/Created

### Created:
- `services/FirebaseMessagingService.kt`
- `utils/NotificationHelper.kt`

### Modified:
- `app/build.gradle.kts` - Added firebase-messaging dependency
- `AndroidManifest.xml` - Added permissions and FCM service
- `strings.xml` - Added notification strings
- `MainActivity.kt` - Added permission request and FCM token retrieval
