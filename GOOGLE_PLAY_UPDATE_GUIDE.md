# 🚀 Step-by-Step Guide: Update Google Play Release with New AAB

## 📋 **Overview**
You need to update your Google Play Console release with the new AAB file (`app-release-api35.aab`) that targets API level 35 to fix the API level requirement error.

---

## 🎯 **Step 1: Access Google Play Console**

### **1.1 Open Google Play Console**
1. **Go to:** https://play.google.com/console/
2. **Sign in** with your Google developer account
3. **Select your app:** "Athreya's Sums - Math Workout Game"

### **1.2 Navigate to Release Management**
1. **Click:** "Release" in the left sidebar
2. **Select:** "Production" (or "Testing" if you want to test first)

---

## 🔄 **Step 2: Create New Release (Option A - Recommended)**

### **2.1 Start New Release**
1. **Click:** "Create new release" button
2. **You'll see:** App bundle upload section

### **2.2 Upload New AAB File**
1. **Click:** "Browse files" or drag-and-drop area
2. **Navigate to:** `/Users/hari/Documents/haricode/AthreyasSums/play_store_assets/`
3. **Select:** `app-release-api35.aab`
4. **Wait for upload:** File will process automatically

### **2.3 Verify Upload Success**
You should see:
```
✅ app-release-api35.aab
   Size: ~19.6 MB
   Version: 1.0 (1)
   Target API: 35
   Status: Ready
```

### **2.4 Add Release Notes (Optional)**
```
What's new in this version:
• Updated to target Android API level 35 for enhanced security and performance
• Optimized for latest Android versions
• Bug fixes and improvements
```

### **2.5 Review and Rollout**
1. **Click:** "Review release"
2. **Check:** All information is correct
3. **Select rollout percentage:** 100% (full rollout) or start with smaller %
4. **Click:** "Start rollout to production"

---

## 🔄 **Step 3: Replace Existing Release (Option B - If you have existing draft)**

### **3.1 Edit Current Release**
1. **Find:** Current draft or existing release
2. **Click:** "Edit release"

### **3.2 Remove Old AAB**
1. **Find:** Existing AAB file (app-release.aab)
2. **Click:** "Remove" or trash icon next to old file
3. **Confirm:** Removal

### **3.3 Upload New AAB**
1. **Click:** "Browse files" 
2. **Select:** `app-release-api35.aab`
3. **Wait:** For processing to complete

### **3.4 Save and Continue**
1. **Click:** "Save"
2. **Click:** "Review release"
3. **Click:** "Start rollout to production"

---

## 📱 **Step 4: Version Management (If needed)**

### **4.1 Update Version Code (If required)**
If Google requires a higher version code:

1. **Open:** `/Users/hari/Documents/haricode/AthreyasSums/app/build.gradle.kts`
2. **Change:** 
   ```kotlin
   versionCode = 1  // Change to 2
   versionName = "1.0"  // Change to "1.1" if desired
   ```
3. **Rebuild:** Run `gradle bundleRelease` again
4. **Upload:** New AAB file

---

## ⏱️ **Step 5: Expected Timeline**

### **Processing Times:**
- **Upload:** 2-5 minutes
- **Review (if first time):** 1-3 days
- **Review (update):** Few hours to 1 day
- **Live on Play Store:** 2-3 hours after approval

### **What Happens Next:**
1. **Google reviews** your app (automated + manual if needed)
2. **API level 35 requirement** will be satisfied
3. **App goes live** on Play Store
4. **No more API level errors!**

---

## 🛠️ **Troubleshooting Common Issues**

### **Issue: "Version code must be higher"**
**Solution:** 
```kotlin
// In app/build.gradle.kts
versionCode = 2  // Increase by 1
```
Rebuild and upload new AAB.

### **Issue: "Upload failed"**
**Solution:**
1. Check file size (should be ~19.6 MB)
2. Ensure file is not corrupted
3. Try uploading again
4. Check internet connection

### **Issue: "Bundle contains same version"**
**Solution:**
1. Remove existing bundle first
2. Or increase version code
3. Upload new bundle

---

## 📋 **Quick Checklist - Before Upload**

### **✅ Pre-Upload Verification:**
- [ ] **File ready:** `app-release-api35.aab` (19.6 MB)
- [ ] **Signed properly:** With your release keystore
- [ ] **API level:** Targets 35 ✅
- [ ] **Internet:** Stable connection for upload
- [ ] **Google Account:** Signed into Play Console

### **✅ During Upload:**
- [ ] **Select correct file:** `app-release-api35.aab`
- [ ] **Wait for processing:** Don't close browser
- [ ] **Verify details:** API 35, correct version
- [ ] **Add release notes:** Optional but recommended

### **✅ After Upload:**
- [ ] **Review release:** Check all details
- [ ] **Start rollout:** 100% or staged
- [ ] **Monitor status:** Check for approval
- [ ] **Verify live:** Once approved, check Play Store

---

## 🎉 **Success Indicators**

### **Upload Successful:**
```
✅ Bundle uploaded successfully
✅ Target API: 35 (meets requirements)
✅ No validation errors
✅ Ready for rollout
```

### **Release Live:**
```
✅ App approved by Google
✅ Available on Play Store
✅ No API level warnings
✅ Users can download/update
```

---

## 📞 **Need Help?**

### **If Upload Fails:**
1. **Check file integrity:** Re-generate AAB if needed
2. **Verify signing:** Ensure keystore is correct
3. **Try different browser:** Chrome recommended
4. **Contact support:** Google Play Developer Support

### **Files You Need:**
```
📁 /Users/hari/Documents/haricode/AthreyasSums/play_store_assets/
  ├── app-release-api35.aab ← **USE THIS FILE**
  ├── app-release.aab ← (old file, don't use)
  └── [other assets]
```

**Your new AAB file fixes the API level 35 requirement and is ready for immediate upload!** 🚀

---

## 🔗 **Quick Links:**
- **Google Play Console:** https://play.google.com/console/
- **Your AAB File:** `play_store_assets/app-release-api35.aab`
- **Release Management:** Play Console → Release → Production

**Ready to upload? Follow the steps above and your API level error will be resolved!** ✅