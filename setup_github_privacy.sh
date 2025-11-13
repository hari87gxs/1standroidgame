#!/bin/bash

# 🔒 GitHub Privacy Policy Setup Script
# This script will help you upload your privacy policy to GitHub Pages

echo "🔒 Setting up Privacy Policy for GitHub Pages..."
echo "================================================"

# Step 1: Check if we're in the right directory
if [ ! -f "privacy_policy.html" ]; then
    echo "❌ Error: privacy_policy.html not found in current directory"
    echo "Please run this script from the AthreyasSums directory"
    exit 1
fi

echo "✅ Found privacy_policy.html"

# Step 2: Create a temporary directory for the privacy repo
TEMP_DIR="temp_privacy_repo"
if [ -d "$TEMP_DIR" ]; then
    echo "🧹 Cleaning up existing temporary directory..."
    rm -rf "$TEMP_DIR"
fi

echo "📁 Creating temporary directory for privacy repo..."
mkdir "$TEMP_DIR"
cd "$TEMP_DIR"

# Step 3: Initialize git repo
echo "🎯 Initializing Git repository..."
git init

# Step 4: Copy privacy policy
echo "📄 Adding privacy policy file..."
cp "../privacy_policy.html" .

# Step 5: Create a nice README
echo "📝 Creating README.md..."
cat > README.md << 'EOF'
# Athreya's Sums - Privacy Policy

This repository hosts the privacy policy for **Athreya's Sums - Math Workout Game**.

## 📋 Privacy Policy

View the privacy policy: [privacy_policy.html](privacy_policy.html)

## 🎮 About Athreya's Sums

Athreya's Sums is a completely private, offline math workout game designed for all ages. We don't collect any personal information - everything stays on your device.

## 🔒 Privacy Highlights

- ✅ **No data collection** - Zero personal information collected
- ✅ **Local storage only** - All progress saved on your device
- ✅ **Child-safe** - Safe for users under 13
- ✅ **No third-party services** - No analytics, ads, or tracking
- ✅ **Complete privacy** - You own your data

## 📱 Download

Available on Google Play Store (coming soon)

---
*This privacy policy complies with Google Play Store requirements and applicable privacy laws.*
EOF

# Step 6: Add files to git
echo "📦 Adding files to git..."
git add .

# Step 7: Commit
echo "💾 Creating initial commit..."
git commit -m "Add privacy policy for Athreya's Sums app

- Complete privacy policy for Google Play Store compliance
- No data collection, local storage only
- Child-safe design for all ages
- Professional HTML formatting"

# Step 8: Show next steps
echo ""
echo "🎉 SUCCESS! Your privacy policy is ready!"
echo "========================================"
echo ""
echo "📋 NEXT STEPS:"
echo ""
echo "1. 🔗 **Add GitHub remote:**"
echo "   git remote add origin https://github.com/YOUR_USERNAME/athreya-sums-privacy.git"
echo ""
echo "2. 📤 **Push to GitHub:**" 
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. 🌐 **Enable GitHub Pages:**"
echo "   - Go to: https://github.com/YOUR_USERNAME/athreya-sums-privacy"
echo "   - Click: Settings → Pages"
echo "   - Source: 'Deploy from a branch'"
echo "   - Branch: 'main'"
echo "   - Click: Save"
echo ""
echo "4. 🎯 **Your Privacy Policy URL will be:**"
echo "   https://YOUR_USERNAME.github.io/athreya-sums-privacy/privacy_policy.html"
echo ""
echo "📁 Files ready in: $(pwd)"
echo "📄 Preview your privacy policy: open privacy_policy.html"
echo ""
echo "💡 **Replace 'YOUR_USERNAME' with your actual GitHub username**"
echo ""

# Step 9: Ask if they want to see the privacy policy
echo "🔍 Would you like to preview the privacy policy? (y/n)"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
    echo "🌐 Opening privacy policy in browser..."
    open privacy_policy.html
fi

echo ""
echo "✅ Setup complete! Follow the steps above to publish your privacy policy."