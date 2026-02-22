# 📱 SafeFlow - Auto-Update System Guide

## ✅ What Was Implemented

### 1. **UI Fixed - Visible Button**
   - Large, centered button: "ACTIVER LA PROTECTION"
   - Version display: "v1.0"
   - Clean, simple layout
   - Blue theme (#2196F3)

### 2. **Auto-Update System**
   - Checks for updates on app startup
   - Reads version info from GitHub
   - Shows French dialog if update available
   - Downloads APK via DownloadManager

---

## 📁 Files Created/Modified

### New Files:
1. **UpdateChecker.kt** - Update checking logic
   - Fetches version.json from GitHub
   - Compares versions
   - Downloads APK
   
2. **version.json** - Version info file
   - Current version: 1.0
   - Download URL
   - Release notes

### Modified Files:
1. **MainActivity.kt** - Added update check
2. **activity_main.xml** - Fixed blank screen
3. **AndroidManifest.xml** - Added permissions

---

## 🔧 How the Update System Works

### Step 1: App Startup
```kotlin
onCreate() → checkForUpdates()
```

### Step 2: Check GitHub
```kotlin
UpdateChecker fetches:
https://raw.githubusercontent.com/Simondbf/SafeFlow/main/version.json
```

### Step 3: Compare Versions
```kotlin
Current: 1.0
Latest: 1.1 (example)
→ isNewerVersion() = true
```

### Step 4: Show Dialog
```
Title: "Mise à jour disponible"
Message: "Une nouvelle version (1.1) est prête..."
Buttons: [Oui] [Non]
```

### Step 5: Download (if "Oui")
```kotlin
DownloadManager downloads APK to:
/storage/emulated/0/Download/SafeFlow-update.apk
```

---

## 📝 How to Release a New Version

### Method 1: Update version.json on GitHub

1. **Edit version.json** on GitHub:
   ```json
   {
     "version": "1.1",
     "download_url": "https://github.com/Simondbf/SafeFlow/releases/download/v1.1/SafeFlow-v1.1.apk",
     "release_notes": "Ajout de nouvelles fonctionnalités"
   }
   ```

2. **Create GitHub Release**:
   - Go to: https://github.com/Simondbf/SafeFlow/releases
   - Click "Create a new release"
   - Tag: `v1.1`
   - Title: `SafeFlow v1.1`
   - Upload APK file: `SafeFlow-v1.1.apk`
   - Publish release

3. **Users Get Update**:
   - Users open app
   - Dialog appears automatically
   - Tap "Oui" to download
   - Install new APK

### Method 2: Direct version.json Update

1. Edit `/app/version.json`:
   ```json
   {
     "version": "1.2",
     "download_url": "YOUR_DIRECT_APK_URL",
     "release_notes": "Description des changements"
   }
   ```

2. Commit and push:
   ```bash
   git add version.json
   git commit -m "Bump version to 1.2"
   git push
   ```

3. Upload APK to the URL specified in `download_url`

---

## 🎯 Version Number Format

**Format**: `MAJOR.MINOR.PATCH`
- **1**.0.0 = Major release
- 1.**1**.0 = Minor update
- 1.0.**1** = Patch/bugfix

**Examples:**
- `1.0` → `1.1` = Minor update (new features)
- `1.1` → `2.0` = Major update (big changes)
- `1.0` → `1.0.1` = Patch (bug fixes)

---

## 📱 User Experience

### When Update Available:
1. User opens SafeFlow
2. Dialog appears immediately
3. Title: "Mise à jour disponible"
4. Message: "Une nouvelle version (1.1) est prête. Voulez-vous la télécharger ?"
5. User taps "Oui"
6. Notification: "Téléchargement de la mise à jour..."
7. Download completes
8. User taps notification to install

### When No Update:
1. User opens SafeFlow
2. No dialog (silent check)
3. App opens normally

---

## 🔐 Permissions Added

```xml
<!-- For checking updates online -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- For installing downloaded APK -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<!-- For saving APK to Downloads -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

---

## 🧪 Testing the Update System

### Test Update Flow:

1. **Change version.json to 1.1**:
   ```json
   {
     "version": "1.1",
     "download_url": "https://example.com/test.apk",
     "release_notes": "Test update"
   }
   ```

2. **Push to GitHub**:
   ```bash
   git add version.json
   git commit -m "Test update to 1.1"
   git push
   ```

3. **Open SafeFlow app**:
   - Dialog should appear
   - Shows version 1.1

4. **Reset for production**:
   ```json
   {
     "version": "1.0",
     "download_url": "...",
     "release_notes": "..."
   }
   ```

---

## 📊 Update Check Logic

```kotlin
// Current version in UpdateChecker.kt
private const val CURRENT_VERSION = "1.0"

// Fetch latest from GitHub
val latestVersion = json.getString("version")

// Compare
if (isNewerVersion(CURRENT_VERSION, latestVersion)) {
    // Show dialog
}
```

### Version Comparison Examples:
- `1.0` vs `1.1` → Update available ✅
- `1.0` vs `1.0` → No update ❌
- `1.0` vs `2.0` → Update available ✅
- `1.1` vs `1.0` → No update ❌

---

## 🚀 Quick Start for Next Version

### To release version 1.1:

1. **Build new APK** via GitHub Actions
2. **Download APK** from Artifacts
3. **Create GitHub Release**:
   - Tag: `v1.1`
   - Upload APK as `SafeFlow-v1.1.apk`
4. **Update version.json**:
   ```json
   {
     "version": "1.1",
     "download_url": "https://github.com/Simondbf/SafeFlow/releases/download/v1.1/SafeFlow-v1.1.apk",
     "release_notes": "Nouvelle version avec améliorations"
   }
   ```
5. **Commit and push**
6. **Done!** Users will see update dialog

---

## 🎨 UI Elements

### Button:
- Text: "ACTIVER LA PROTECTION"
- Color: Blue (#2196F3)
- Action: Opens Accessibility Settings

### Version Display:
- Text: "v1.0"
- Color: Gray (#757575)
- Auto-updates when you change version

---

## 📞 Troubleshooting

### Update Dialog Not Showing:
1. Check internet connection
2. Verify version.json exists on GitHub
3. Check logs: `adb logcat | grep UpdateChecker`

### Download Fails:
1. Check download URL is correct
2. Verify INTERNET permission granted
3. Check notification for download status

### Can't Install Downloaded APK:
1. Enable "Install from Unknown Sources"
2. Check REQUEST_INSTALL_PACKAGES permission

---

## ✅ Summary

**Implemented:**
- ✅ UI fixed with visible button
- ✅ Auto-update check on startup
- ✅ French dialog for updates
- ✅ DownloadManager integration
- ✅ Version.json system
- ✅ All permissions added

**Files:**
- ✅ MainActivity.kt (73 lines)
- ✅ UpdateChecker.kt (90 lines)
- ✅ activity_main.xml (fixed layout)
- ✅ version.json (update info)
- ✅ AndroidManifest.xml (permissions)

**Ready to use!** 🚀

