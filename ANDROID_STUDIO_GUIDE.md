# 📱 Step-by-Step Android Studio Guide
# Athreya's Sums - Math Workout Game

## 🚀 Phase 1: Opening the Project

### Step 1: Launch Android Studio
1. **Open Android Studio** on your Mac
2. If you see a "Welcome" screen, proceed to Step 2
3. If you have another project open, go to **File → Close Project** first

### Step 2: Import Your Project
1. On the Welcome screen, click **"Open"** (or **File → Open** if in main window)
2. Navigate to: `/Users/hari/Documents/haricode/AthreyasSums`
3. **Select the folder** `AthreyasSums` (not any subfolder)
4. Click **"Open"**

### Step 3: Wait for Initial Setup
1. Android Studio will show **"Opening Project..."**
2. You'll see **"Gradle sync in progress..."** at the bottom
3. **Wait patiently** - this can take 2-5 minutes on first open
4. You might see download progress for dependencies

## ⚙️ Phase 2: Project Sync & Setup

### Step 4: Handle First-Time Setup Prompts
You might see one or more of these dialogs:

**If you see "Gradle Wrapper":**
- Click **"OK"** to use the Gradle wrapper

**If you see "SDK Setup":**
- Android Studio might prompt to download/update SDK components
- Click **"Accept"** and let it download

**If you see "Trust Project":**
- Click **"Trust Project"** since this is your own code

### Step 5: Wait for Gradle Sync to Complete
Watch the bottom status bar:
1. You'll see **"Gradle sync in progress..."**
2. Wait for it to change to **"Gradle sync finished in Xs"**
3. If sync fails, see Troubleshooting section below

### Step 6: Verify Project Structure
In the Project Explorer (left panel), you should see:
```
📁 AthreyasSums
├── 📁 app
│   ├── 📁 src
│   │   └── 📁 main
│   │       ├── 📁 java/com/athreya/mathworkout
│   │       │   ├── MainActivity.kt
│   │       │   ├── 📁 data
│   │       │   ├── 📁 ui
│   │       │   └── 📁 viewmodel
│   │       └── 📁 res
│   │           ├── 📁 values (themes.xml, colors.xml)
│   │           └── 📁 values-night
│   └── build.gradle.kts
└── build.gradle.kts
```

## 🔧 Phase 3: Clean & Build

### Step 7: Clean the Project (Important!)
1. Go to **Build** menu
2. Click **"Clean Project"**
3. Wait for cleaning to complete (usually 30 seconds)

### Step 8: Sync Project with Gradle
1. Go to **File** menu
2. Click **"Sync Project with Gradle Files"**
3. Wait for sync to complete

### Step 9: Rebuild Project
1. Go to **Build** menu
2. Click **"Rebuild Project"**
3. Watch the Build Output panel (bottom) for progress
4. **Success**: You should see "BUILD SUCCESSFUL"
5. **Failure**: See troubleshooting section below

## 📱 Phase 4: Setup Emulator & Run

### Step 10: Setup Android Emulator
1. In the top toolbar, look for the device dropdown (next to the green ▶️ button)
2. Click the dropdown
3. If you see "No Devices", click **"Create Device"**

**Create New Emulator:**
1. Click **"Create Device"**
2. Choose **"Phone"** category
3. Select **"Pixel 6"** or **"Pixel 7"** (recommended)
4. Click **"Next"**
5. Select **API Level 34** (latest stable)
6. Click **"Next"**
7. Name it "Pixel_API_34" or similar
8. Click **"Finish"**
9. Wait for emulator to be created

### Step 11: Start the Emulator
1. In the device dropdown, select your newly created emulator
2. Click the **▶️ (Run)** button next to it
3. Wait for emulator to boot up (first time takes 2-3 minutes)
4. You'll see the Android home screen when ready

### Step 12: Run Your App
1. Make sure your emulator is selected in the device dropdown
2. Click the big green **▶️ (Run 'app')** button
3. **First run might take 2-4 minutes** as it compiles and installs
4. Watch for **"Installing APK"** message in the bottom panel

## 🎯 Phase 5: Expected Results

### If Everything Works:
1. Your app should launch automatically on the emulator
2. You should see the **"Athreya's Sums"** home screen
3. You can navigate between different game modes
4. The app should be fully functional!

### App Features to Test:
1. **Home Screen**: Should show game mode options
2. **Settings Screen**: Should allow difficulty selection
3. **Game Screen**: Should generate math questions
4. **High Scores**: Should track your best scores

## 🚨 Troubleshooting Common Issues

### If Gradle Sync Fails:

**Error: "Could not resolve dependencies"**
1. Go to **File → Settings** (or **Android Studio → Preferences** on Mac)
2. Navigate to **Build, Execution, Deployment → Gradle**
3. Make sure **"Use Gradle from: gradle-wrapper.properties"** is selected
4. Click **"Apply"** and **"OK"**
5. Try **File → Sync Project with Gradle Files** again

**Error: "SDK not found"**
1. Go to **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Make sure **Android 14.0 (API 34)** is installed
3. If not, check the box and click **"Apply"**

### If Build Fails:

**Theme-related errors (if our fix didn't work):**
1. Go to **Build → Clean Project**
2. Go to **File → Invalidate Caches → Invalidate and Restart**
3. Wait for Android Studio to restart and try again

**Out of memory errors:**
1. Go to **Help → Edit Custom VM Options**
2. Add these lines:
   ```
   -Xmx4096m
   -XX:MaxMetaspaceSize=512m
   ```
3. Restart Android Studio

### If App Crashes on Launch:

1. Look at the **Logcat** panel (bottom of screen)
2. Filter by **"Error"** to see crash details
3. Most likely causes:
   - Missing permissions (should auto-resolve)
   - Theme issues (should be fixed by our changes)

### If Emulator is Slow:

1. Make sure **Hardware Acceleration** is enabled:
   - **Tools → AVD Manager**
   - Click **pencil icon** next to your emulator
   - Click **"Advanced Settings"**
   - Set **Graphics** to **"Hardware - GLES 2.0"**

## 📋 Quick Checklist

Before running your app, verify:
- [ ] ✅ Project opened successfully
- [ ] ✅ Gradle sync completed without errors
- [ ] ✅ Project rebuilt successfully  
- [ ] ✅ Emulator is running
- [ ] ✅ Device is selected in dropdown
- [ ] ✅ No red error indicators in code

## 🎮 Your Math Game Features

Once running, you should be able to:
- [ ] ✅ Choose game modes (Addition, Multiplication, Mixed, Brain Teasers)
- [ ] ✅ Select difficulty levels (Easy, Medium, Hard)
- [ ] ✅ Play math questions with multiple choice answers
- [ ] ✅ Track scores and see progress
- [ ] ✅ View high scores
- [ ] ✅ Switch between light/dark themes

## 🚀 Success Indicators

**You know it's working when:**
1. ✅ App installs without errors
2. ✅ Home screen appears with game options
3. ✅ You can navigate between screens
4. ✅ Math questions generate properly
5. ✅ Scoring system works
6. ✅ No crashes during normal use

---

**Remember**: The first run always takes longer! Be patient during the initial build and installation process. Once it's running, subsequent launches will be much faster.

**Need help?** If you encounter any specific errors, copy the exact error message from Android Studio and I can help you troubleshoot!

Good luck with your Math Workout Game! 🎯📱