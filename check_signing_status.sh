#!/bin/bash

# 🔐 Signed APK Preparation Checker
echo "🔐 SIGNED APK PREPARATION STATUS"
echo "================================"

cd /Users/hari/Documents/haricode/AthreyasSums

# Check if Android Studio project is ready
echo ""
echo "📱 PROJECT STATUS:"
if [ -f "app/build.gradle.kts" ]; then
    echo "✅ Build configuration exists"
else
    echo "❌ Build configuration missing"
fi

if [ -f "app/proguard-rules.pro" ]; then
    echo "✅ ProGuard rules configured"
else
    echo "❌ ProGuard rules missing"
fi

# Check release build type
if grep -q "isMinifyEnabled = true" app/build.gradle.kts; then
    echo "✅ Release optimization enabled"
else
    echo "❌ Release optimization not configured"
fi

# Check keystore status
echo ""
echo "🔑 KEYSTORE STATUS:"
if [ -f "release-keystore.jks" ]; then
    echo "✅ Keystore exists"
    ls -lh release-keystore.jks
else
    echo "❌ Keystore not created yet"
    echo "   → Create via Android Studio: Build → Generate Signed Bundle"
fi

# Check assets directory
echo ""
echo "📁 ASSETS STATUS:"
if [ -d "play_store_assets" ]; then
    echo "✅ Assets directory exists"
    echo "   Files ready:"
    find play_store_assets -name "*.png" -o -name "*.txt" -o -name "*.aab" | sort
else
    echo "❌ Assets directory missing"
fi

# Check for generated AAB
echo ""
echo "📦 BUILD STATUS:"
if [ -f "play_store_assets/app-release.aab" ]; then
    echo "✅ Signed AAB ready!"
    ls -lh play_store_assets/app-release.aab
else
    echo "❌ Signed AAB not generated yet"
    echo "   → Run: Build → Generate Signed Bundle in Android Studio"
fi

echo ""
echo "🎯 NEXT STEPS:"
if [ ! -f "release-keystore.jks" ]; then
    echo "1️⃣ Create keystore via Android Studio"
    echo "2️⃣ Generate signed bundle"
elif [ ! -f "play_store_assets/app-release.aab" ]; then
    echo "1️⃣ Generate signed bundle in Android Studio"
else
    echo "🎉 Ready to upload to Google Play Store!"
fi

echo ""
echo "🚨 REMEMBER:"
echo "   • Backup your keystore file securely"
echo "   • Save keystore passwords safely" 
echo "   • Never commit keystore to git"
echo ""