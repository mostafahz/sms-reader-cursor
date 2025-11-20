# 🚀 Build Instructions - SMS Finance Tracker

## Quick Start

This document provides step-by-step instructions to build your APK using GitHub Actions (no Android Studio required!).

---

## Prerequisites

1. **Git installed** on your computer
2. **GitHub account** (free)
3. **Your Android phone** to install the APK

---

## Step-by-Step Build Process

### 1. Create GitHub Repository

1. Go to [GitHub](https://github.com)
2. Click the **+** icon in the top right
3. Select **New repository**
4. Name it: `sms-finance-tracker` (or any name you prefer)
5. Leave it **Public** (or Private if you have a paid account)
6. **DO NOT** initialize with README, .gitignore, or license
7. Click **Create repository**

### 2. Upload Project to GitHub

Open PowerShell in your project directory (`D:\Mine\Apps\sms-reader-cursor`) and run:

```powershell
# Initialize git
git init

# Add all files
git add .

# Commit the files
git commit -m "Initial commit: SMS Finance Tracker with all features"

# Set main branch
git branch -M main

# Add your GitHub repository (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/sms-finance-tracker.git

# Push to GitHub
git push -u origin main
```

**Example:**
If your GitHub username is `johnsmith`, the command would be:
```powershell
git remote add origin https://github.com/johnsmith/sms-finance-tracker.git
```

### 3. GitHub Actions Will Build Automatically

Once you push to GitHub:

1. GitHub Actions will **automatically start building** your APK
2. Go to your repository page on GitHub
3. Click the **Actions** tab at the top
4. You'll see a workflow called "Android Build" running

**Build Status:**
- 🟡 **Yellow circle** = Building in progress (takes ~5-10 minutes)
- ✅ **Green checkmark** = Build successful
- ❌ **Red X** = Build failed (check logs for errors)

### 4. Download Your APK

Once the build is complete (green checkmark):

1. Click on the workflow run (e.g., "Initial commit: SMS Finance...")
2. Scroll down to the **Artifacts** section
3. Click on **app-debug** to download a ZIP file
4. Extract the ZIP file
5. You'll find `app-debug.apk` inside

### 5. Install on Your Phone

**Method 1: Direct Transfer**
1. Connect your phone to your computer via USB
2. Copy `app-debug.apk` to your phone's Downloads folder
3. On your phone, open the file manager
4. Navigate to Downloads and tap `app-debug.apk`
5. If prompted, enable "Install from Unknown Sources"
6. Tap "Install"

**Method 2: Cloud Transfer**
1. Upload `app-debug.apk` to Google Drive, Dropbox, etc.
2. Download on your phone
3. Tap to install

**Method 3: Email**
1. Email the APK to yourself
2. Open email on phone
3. Download and install

---

## Troubleshooting

### "git: command not found"
- Install Git from: https://git-scm.com/download/win
- Restart PowerShell after installation

### Build Failed on GitHub Actions
Common issues:
- **Missing files**: Make sure all files are committed
  ```powershell
  git status  # Check what files are uncommitted
  git add .   # Add all files
  git commit -m "Fix: Add missing files"
  git push
  ```

### "Install Blocked" on Phone
- Go to: Settings → Security → Unknown Sources
- Enable "Allow from this source"
- Try installing again

### Permissions Not Granted
- After first launch, go to: Settings → Apps → SMS Finance Tracker → Permissions
- Manually enable SMS, Notifications
- Restart app

---

## Updating the App

When you make changes to the code:

```powershell
# Check what changed
git status

# Add all changes
git add .

# Commit with a message
git commit -m "Update: Improved category detection"

# Push to GitHub
git push
```

GitHub Actions will automatically build a new APK!

---

## Project File Structure

```
sms-reader-cursor/
├── .github/
│   └── workflows/
│       └── android-build.yml          # GitHub Actions workflow
├── app/
│   ├── build.gradle.kts               # App dependencies
│   ├── proguard-rules.pro             # ProGuard config
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # App permissions & config
│           ├── java/                  # Kotlin source code
│           └── res/                   # Resources (layouts, strings, icons)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle version
├── build.gradle.kts                   # Root build config
├── settings.gradle.kts                # Project settings
├── gradle.properties                  # Gradle properties
├── .gitignore                         # Git ignore rules
├── README.md                          # Project overview
├── PROJECT_PLAN.md                    # Detailed planning
├── TECHNICAL_SPECS.md                 # Technical documentation
├── APPROVAL_CHECKLIST.md              # Feature checklist
├── SMS_EXAMPLES.md                    # SMS pattern examples
├── walkthrough.md                     # User walkthrough
└── BUILD_INSTRUCTIONS.md              # This file
```

---

## What Gets Built

The GitHub Actions workflow:
1. Checks out your code
2. Sets up Java 17
3. Grants execute permissions to `gradlew`
4. Runs `./gradlew assembleDebug`
5. Uploads the APK as an artifact

The resulting APK:
- **Filename**: `app-debug.apk`
- **Size**: ~5-10 MB
- **Signed**: Debug keystore (for testing)
- **Minimum Android**: 8.0 (API 26)
- **Target Android**: 14 (API 34)

---

## Building Locally (Optional)

If you want to build on your computer without Android Studio:

### Requirements
- Java Development Kit (JDK) 17
- Command line access

### Commands
```powershell
# Make gradlew executable (on Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug

# Find APK at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## Additional Notes

### Release vs Debug APK
- **Debug APK**: For testing, uses debug keystore, larger size
- **Release APK**: For production, requires signing key, optimized

For personal use, debug APK is fine!

### Signing for Google Play (Future)
If you want to publish on Play Store:
1. Generate a keystore
2. Update `build.gradle.kts` with signing config
3. Build release APK
4. Upload to Play Console

---

## Support

### Common Issues

**Q: Can I use this without GitHub?**
A: Yes! Use the local build method, but you'll need JDK 17.

**Q: How do I get JDK 17?**
A: Download from: https://adoptium.net/

**Q: The APK is 10MB, is that normal?**
A: Yes! It includes all dependencies (Room, Compose, CSV library).

**Q: Can I reduce the size?**
A: In release builds with ProGuard enabled, size reduces to ~3-5MB.

**Q: How do I update the app version?**
A: Edit `app/build.gradle.kts`:
```kotlin
versionCode = 2        // Increment this
versionName = "1.0.1"  // Update version string
```

---

## Success Checklist

Before pushing to GitHub, verify:
- ✅ All files are present in the directory
- ✅ `app/build.gradle.kts` exists
- ✅ `settings.gradle.kts` exists
- ✅ `.github/workflows/android-build.yml` exists
- ✅ `gradlew` script exists (for Linux/Mac)
- ✅ `gradle/wrapper/gradle-wrapper.properties` exists

---

## Ready to Build! 🎉

Follow the steps above, and in 10 minutes you'll have your APK ready to install!

**Questions?** Check the [walkthrough.md](walkthrough.md) for detailed usage instructions.

**Happy Building! 📱💰**

