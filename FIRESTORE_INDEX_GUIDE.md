# Firestore Index Deployment Guide

## What is this?

The Firestore composite index is required for the "Discover Public Groups" feature to work properly. It allows Firestore to efficiently query groups that are public and sort them by member count.

## Option 1: Deploy via Firebase CLI (Recommended)

1. Install Firebase CLI (if not already installed):
   ```bash
   npm install -g firebase-tools
   ```

2. Login to Firebase:
   ```bash
   firebase login
   ```

3. Initialize Firebase in your project (if not already done):
   ```bash
   cd /Users/hari/Documents/haricode/AthreyasSums
   firebase init firestore
   ```
   - Select your project: `athreyas-math-workout`
   - Use default options for Firestore rules and indexes

4. Deploy the indexes:
   ```bash
   firebase deploy --only firestore:indexes
   ```

## Option 2: Create via Firebase Console

1. Open Firebase Console: https://console.firebase.google.com/project/athreyas-math-workout/firestore/indexes

2. Click "Add Index"

3. Configure the index:
   - Collection ID: `groups`
   - Fields to index:
     - Field: `isPublic`, Order: Ascending
     - Field: `memberCount`, Order: Descending
   - Query scope: Collection

4. Click "Create"

5. Wait for the index to build (usually takes 1-2 minutes)

## Option 3: Use the Auto-Generated Link

When you first encounter the error, Firebase provides a direct link to create the index. You can click that link and it will pre-fill all the settings for you.

## Verification

After creating the index, the "Discover Public Groups" feature will work without errors. You can verify by:

1. Opening the Groups screen
2. Clicking the globe icon (Discover Public Groups)
3. The public groups should load without any error messages

## Note for Development

The current code has been updated to use the proper orderBy query. If the index is not yet created, you may see an error. The app will still work for creating and joining groups via group codes - only the "Discover Public Groups" feature requires this index.
