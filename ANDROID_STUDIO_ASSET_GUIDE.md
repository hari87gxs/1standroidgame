# Android Studio Asset Studio Guide
## Generating High-Resolution App Icon for "Athreya's Sums"

### 🎯 Goal
Create 512x512 high-resolution icon + all Android app icon sizes using Android Studio's Asset Studio

### 📱 What We'll Generate
- ✅ 512x512 PNG for Google Play Store
- ✅ All Android density folders (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- ✅ Adaptive icons (foreground + background)
- ✅ Round icons for compatible devices
- ✅ Legacy icons for older Android versions

### 🛠 Step-by-Step Process

#### Step 1: Open Project in Android Studio
```bash
# Method 1: Command Line
open -a "Android Studio" /Users/hari/Documents/haricode/AthreyasSums

# Method 2: Android Studio Menu
# File → Open → Navigate to AthreyasSums folder → Open
```

#### Step 2: Access Asset Studio
1. **In Project Panel (left side):**
   - Navigate to `app/src/main/res`
   - Right-click on `res` folder
   - Select: `New → Image Asset`

#### Step 3: Configure Asset Studio
**Icon Type Tab:**
- ✅ Select: "Launcher Icons (Adaptive and Legacy)"

**Foreground Layer Tab:**
- ✅ Asset Type: "Image" 
- ✅ Path: Browse to your `ic_launcher_foreground.xml`
- OR create new design in Asset Studio

**Background Layer Tab:**
- ✅ Asset Type: "Image"
- ✅ Path: Browse to your `ic_launcher_background.xml` 
- OR use solid color: #1976D2

#### Step 4: Preview and Generate
1. **Preview Window shows:**
   - Circle, Square, Rounded Square previews
   - Different Android versions
   - Various screen densities

2. **Click "Next" when satisfied**

3. **Confirm Generation:**
   - Shows all files that will be created
   - Click "Finish" to generate

#### Step 5: Verify Generated Files
**Check these folders were created/updated:**
```
app/src/main/res/
├── mipmap-hdpi/
│   ├── ic_launcher.png (72x72)
│   ├── ic_launcher.webp
│   └── ic_launcher_round.png
├── mipmap-mdpi/
│   ├── ic_launcher.png (48x48)
│   └── ic_launcher_round.png
├── mipmap-xhdpi/
│   ├── ic_launcher.png (96x96)
│   └── ic_launcher_round.png
├── mipmap-xxhdpi/
│   ├── ic_launcher.png (144x144)
│   └── ic_launcher_round.png
├── mipmap-xxxhdpi/
│   ├── ic_launcher.png (192x192)
│   └── ic_launcher_round.png
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml
    └── ic_launcher_round.xml
```

#### Step 6: Export High-Resolution Version
**For Google Play Store (512x512):**
1. In Asset Studio, look for "Web" tab or "Export" option
2. OR manually create from xxxhdpi version:
   - Take `mipmap-xxxhdpi/ic_launcher.png` (192x192)
   - Use image editor to upscale to 512x512
   - Save as `play_store_icon.png`

### 🎨 Alternative: Manual High-Res Creation

If Asset Studio doesn't provide 512x512 directly:

#### Option A: Vector to PNG Conversion
```bash
# Install librsvg (if needed)
brew install librsvg

# Convert your vector icon to 512x512 PNG
# (We'll create an SVG version of your icon first)
```

#### Option B: Online Conversion
1. Visit: https://cloudconvert.com/svg-to-png
2. Upload your icon design
3. Set dimensions: 512x512
4. Download result

### ✅ Success Criteria
- [ ] Android Studio opens project successfully
- [ ] Asset Studio launches from res folder
- [ ] All mipmap folders contain new icons
- [ ] App builds without errors
- [ ] Icon appears correctly in app
- [ ] 512x512 version ready for Play Store

### 🚨 Troubleshooting
**If Android Studio won't open project:**
- Ensure Android SDK is installed
- Check build.gradle files are valid
- Try "Invalidate Caches and Restart"

**If Asset Studio shows errors:**
- Verify XML icon files are valid
- Check file paths are correct
- Ensure sufficient permissions

**If build fails after icon update:**
- Clean and rebuild project
- Check for duplicate resource names
- Verify all icon files generated correctly