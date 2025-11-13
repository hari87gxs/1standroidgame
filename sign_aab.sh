#!/bin/bash

# 🔐 Manual AAB Signing Script
# This script will sign your AAB file properly for Google Play Store

echo "🔐 Signing AAB File for Google Play Store..."
echo "============================================"

# Check if unsigned AAB exists
UNSIGNED_AAB="app/build/outputs/bundle/release/app-release.aab"
SIGNED_AAB="play_store_assets/app-release-api35-signed.aab"
KEYSTORE="release-keystore.jks"

if [ ! -f "$UNSIGNED_AAB" ]; then
    echo "❌ Error: Unsigned AAB not found at $UNSIGNED_AAB"
    echo "Building unsigned AAB first..."
    gradle assembleRelease bundleRelease
    
    if [ ! -f "$UNSIGNED_AAB" ]; then
        echo "❌ Failed to build AAB. Please check build errors."
        exit 1
    fi
fi

echo "✅ Found unsigned AAB: $UNSIGNED_AAB"

# Check if keystore exists
if [ ! -f "$KEYSTORE" ]; then
    echo "❌ Error: Keystore not found at $KEYSTORE"
    echo "Please ensure your keystore file is in the project root"
    exit 1
fi

echo "✅ Found keystore: $KEYSTORE"

# Sign the AAB using jarsigner (works with any Java installation)
echo "🔏 Signing AAB file..."

# First, let's try to list the keystore contents to find the correct alias
echo "🔍 Checking keystore contents..."

# Since keytool isn't available, let's try different common aliases
POSSIBLE_ALIASES=("athreyassums-key" "athreya-sums-key" "release-key" "key0" "androiddebugkey")
KEYSTORE_PASSWORD="AthreyaSums2024!"
KEY_PASSWORD="AthreyaSums2024!"

echo "📋 Trying different key aliases..."

for alias in "${POSSIBLE_ALIASES[@]}"; do
    echo "🔑 Trying alias: $alias"
    
    # Copy unsigned AAB to signed location
    cp "$UNSIGNED_AAB" "$SIGNED_AAB"
    
    # Try to sign with this alias
    if jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
        -keystore "$KEYSTORE" -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEY_PASSWORD" "$SIGNED_AAB" "$alias" 2>/dev/null; then
        
        echo "✅ Successfully signed with alias: $alias"
        
        # Verify the signature
        if jarsigner -verify -verbose "$SIGNED_AAB" 2>/dev/null; then
            echo "✅ Signature verification successful!"
            echo ""
            echo "🎉 SUCCESS! Signed AAB created:"
            echo "📁 File: $SIGNED_AAB"
            echo "🔑 Alias: $alias"
            echo "📊 Size: $(ls -lh "$SIGNED_AAB" | awk '{print $5}')"
            echo ""
            echo "🚀 Ready for Google Play Console upload!"
            exit 0
        else
            echo "❌ Signature verification failed for alias: $alias"
        fi
    else
        echo "❌ Failed to sign with alias: $alias"
        rm -f "$SIGNED_AAB"
    fi
done

echo ""
echo "❌ Could not sign AAB with any known alias"
echo ""
echo "🛠️  ALTERNATIVE SOLUTIONS:"
echo ""
echo "1️⃣  **Use Android Studio (Recommended):**"
echo "   - Open project in Android Studio"
echo "   - Build → Generate Signed Bundle/APK"
echo "   - Select Android App Bundle"
echo "   - Choose your keystore and alias"
echo ""
echo "2️⃣  **Create new keystore:**"
echo "   - Run: keytool -genkey -v -keystore new-keystore.jks ..."
echo "   - Update build.gradle.kts with new details"
echo ""
echo "3️⃣  **Use existing signed AAB:**"
echo "   - File: app/release/app-release.aab (if properly signed)"
echo "   - Check if this file works for upload"
echo ""
echo "📞 Need help? Check the Android Studio signing guide!"