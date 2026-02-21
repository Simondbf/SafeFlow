# 🚀 SafeFlow - Quick Start Guide

## What is SafeFlow?

SafeFlow is a Native Android app that automatically blocks and closes specified apps using Android's AccessibilityService. When you try to open a blocked app (like Bing), SafeFlow immediately closes it by performing a "back" action.

---

## ⚡ Quick Setup (5 minutes)

### Step 1: Open in Android Studio
1. Launch **Android Studio**
2. Select **"Open an existing project"**
3. Navigate to `/app/android/` folder
4. Click **Open**
5. Wait for Gradle sync to complete (~2 minutes first time)

### Step 2: Build & Install
1. Connect your Android phone via USB
   - Enable **USB Debugging** in Developer Options
   - Or use an Android Emulator
2. Click the green **▶️ Run** button in Android Studio
3. Select your device
4. App will install automatically

### Step 3: Grant Permission
1. Open **SafeFlow** app on your phone
2. Tap **"Open Accessibility Settings"** button
3. Find **SafeFlow** in the list
4. Toggle it **ON**
5. Confirm the permission dialog
6. Go back to SafeFlow app
7. The **"Protection Active"** switch will turn ON automatically ✅

### Step 4: Test It!
1. Open the **Bing** app on your phone
2. It should immediately close! 🎯
3. Check Logcat in Android Studio to see logs:
   - `SafeFlow Accessibility Service Connected`
   - `Blocked app detected: com.microsoft.bing - Closing...`
   - `Back action performed on: com.microsoft.bing`

---

## 🎯 Currently Blocked Apps

✅ **Bing Search** (`com.microsoft.bing`)

---

## 🔧 How to Block Different Apps

### Find the Package Name

**Method 1: Using ADB**
```bash
# List all installed apps
adb shell pm list packages

# Search for specific app
adb shell pm list packages | grep chrome
# Output: package:com.android.chrome
```

**Method 2: Using App Inspector**
- Install "App Inspector" from Play Store
- Open it and find your target app
- Copy the package name

**Method 3: From Play Store URL**
- Open app in Play Store
- Look at the URL: `play.google.com/store/apps/details?id=<PACKAGE_NAME>`

### Update the Code

1. Open: `/app/android/app/src/main/java/com/safeflow/MyAccessibilityService.kt`
2. Find lines 14-19:
```kotlin
private val BLOCKED_PACKAGES = listOf(
    "com.microsoft.bing"  // Bing Search (for testing)
    // Add more packages here as needed:
    // "com.android.chrome",
    // "com.instagram.android"
)
```
3. Add or remove package names:
```kotlin
private val BLOCKED_PACKAGES = listOf(
    "com.microsoft.bing",
    "com.android.chrome",        // Block Chrome
    "com.instagram.android",     // Block Instagram
    "com.facebook.katana"        // Block Facebook
)
```
4. Rebuild: **Build → Make Project** (Ctrl+F9)
5. Run again to install updated app

---

## 📁 Project Structure

```
/app/android/
├── build.gradle                          # Project Gradle config
├── settings.gradle                       # Project settings
├── gradle.properties                     # Gradle properties
└── app/
    ├── build.gradle                      # App-level config
    ├── proguard-rules.pro               # ProGuard rules
    └── src/main/
        ├── AndroidManifest.xml           # ⚙️ Service & permission declaration
        ├── java/com/safeflow/
        │   ├── MainActivity.kt           # 🎨 UI and permission handling
        │   └── MyAccessibilityService.kt # 🚫 Core blocking logic (MODIFY HERE)
        └── res/
            ├── layout/
            │   └── activity_main.xml     # UI layout
            ├── values/
            │   ├── strings.xml           # Text strings
            │   └── colors.xml            # Color palette
            └── xml/
                └── accessibility_service_config.xml  # Service configuration
```

**Key File:** `MyAccessibilityService.kt` - Contains the `BLOCKED_PACKAGES` list (line 14)

---

## 🛠️ Build Commands

```bash
cd /app/android

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build + Install
./gradlew clean assembleDebug installDebug

# APK location after build:
# /app/android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐛 Troubleshooting

### ❌ "Service not blocking apps"
**Solution:**
1. Open Android Settings → Accessibility
2. Verify SafeFlow is **ON**
3. Restart your phone
4. Check package name spelling (case-sensitive!)

### ❌ "Gradle sync failed"
**Solution:**
```bash
cd /app/android
chmod +x gradlew
./gradlew clean
```
Then sync again in Android Studio

### ❌ "App crashes on startup"
**Solution:**
1. Check Logcat in Android Studio
2. Look for error messages (red lines)
3. Common issue: Missing SDK components
   - Go to: Tools → SDK Manager
   - Install SDK Platform 24-34

### ❌ "SDK location not found"
**Solution:**
Create `/app/android/local.properties`:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```
Replace with your actual SDK path

---

## 📱 Testing Checklist

- [ ] App installs successfully
- [ ] Main screen shows "SafeFlow" title
- [ ] "Open Accessibility Settings" button works
- [ ] Can enable SafeFlow in Accessibility settings
- [ ] "Protection Active" switch turns ON after permission granted
- [ ] Opening Bing immediately closes it
- [ ] Logcat shows blocking messages
- [ ] Works after screen lock/unlock
- [ ] Works after device restart

---

## 🎯 How It Works

1. **AccessibilityService** monitors all app switches
2. When **TYPE_WINDOW_STATE_CHANGED** event fires:
   - Service checks the package name
   - If it matches `BLOCKED_PACKAGES` list
   - Performs `GLOBAL_ACTION_BACK` (like pressing back button)
3. Result: Blocked app closes immediately! ⚡

---

## 🚀 Future Feature Ideas

The code structure makes it easy to add:

✨ **Allowed Browser Feature**
- Let user choose a "safe" browser
- Block all browsers EXCEPT the chosen one
- Implementation: Remove user's choice from `BLOCKED_PACKAGES`

✨ **Statistics Dashboard**
- Count how many times apps were blocked
- Show blocked apps history
- Display most blocked app

✨ **Schedule-based Blocking**
- Block apps only during work hours
- Different block lists for different times

✨ **Password Protection**
- Require password to disable protection
- Prevent easy bypass

---

## 📚 Key Files to Know

| File | Purpose | When to Edit |
|------|---------|--------------|
| `MyAccessibilityService.kt` | Blocking logic & package list | Add/remove blocked apps |
| `MainActivity.kt` | UI and permission handling | Change interface behavior |
| `activity_main.xml` | Screen layout | Modify UI design |
| `strings.xml` | All text strings | Change app text |
| `AndroidManifest.xml` | App configuration | Add new permissions/services |

---

## ⚠️ Important Notes

- **Battery Usage**: Accessibility services run constantly, slight battery impact
- **Permissions**: Only works with user-granted accessibility permission
- **System Apps**: Some system apps may not be blockable due to Android restrictions
- **Responsible Use**: Accessibility permission is powerful - use ethically

---

## 🎓 Learning Resources

- [Android Accessibility Guide](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Kotlin for Android](https://developer.android.com/kotlin)
- [Android Studio Basics](https://developer.android.com/studio/intro)

---

## 📞 Need Help?

1. **Check Logcat**: Most issues show error messages there
2. **Review BUILD_GUIDE.md**: Detailed troubleshooting steps
3. **Test on real device**: Emulators may have accessibility limitations

---

**You're all set! 🎉**

SafeFlow is now blocking Bing. Modify `BLOCKED_PACKAGES` in `MyAccessibilityService.kt` to block different apps!
