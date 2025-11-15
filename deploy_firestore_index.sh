#!/bin/bash

# Firestore Index Deployment Script
# This script helps deploy the composite index for the groups collection

echo "🔥 Firestore Index Deployment for Athreya's Sums"
echo "================================================"
echo ""

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null
then
    echo "❌ Firebase CLI is not installed."
    echo ""
    echo "To install Firebase CLI, run:"
    echo "  npm install -g firebase-tools"
    echo ""
    echo "Or using curl:"
    echo "  curl -sL https://firebase.tools | bash"
    echo ""
    exit 1
fi

echo "✅ Firebase CLI is installed"
echo ""

# Check if user is logged in
echo "Checking Firebase login status..."
firebase login:list &> /dev/null
if [ $? -ne 0 ]; then
    echo "❌ Not logged in to Firebase"
    echo ""
    echo "Please run: firebase login"
    echo ""
    exit 1
fi

echo "✅ Logged in to Firebase"
echo ""

# Initialize Firestore if needed
if [ ! -f "firebase.json" ]; then
    echo "📝 Initializing Firebase in this directory..."
    echo "Select your project: athreyas-math-workout"
    echo "Accept default options for Firestore rules and indexes"
    echo ""
    firebase init firestore
    echo ""
fi

# Deploy the indexes
echo "🚀 Deploying Firestore indexes..."
echo ""
firebase deploy --only firestore:indexes --project athreyas-math-workout

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCCESS! Firestore index deployed successfully"
    echo ""
    echo "The index will take 1-2 minutes to build."
    echo "You can check the status at:"
    echo "https://console.firebase.google.com/project/athreyas-math-workout/firestore/indexes"
    echo ""
    echo "Once the index is ready, the 'Discover Public Groups' feature will work without errors."
else
    echo ""
    echo "❌ Deployment failed. Please check the error above."
    echo ""
    echo "You can also create the index manually at:"
    echo "https://console.firebase.google.com/project/athreyas-math-workout/firestore/indexes"
    echo ""
    echo "Index configuration:"
    echo "  Collection: groups"
    echo "  Field 1: isPublic (Ascending)"
    echo "  Field 2: memberCount (Descending)"
    echo "  Query scope: Collection"
fi
