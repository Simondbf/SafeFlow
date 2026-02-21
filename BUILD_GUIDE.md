# SafeFlow - Build & Development Guide

## 🏗️ Building the Project

### Prerequisites

1. **Install Android Studio**:
   - Download from: https://developer.android.com/studio
   - Version: Arctic Fox (2020.3.1) or later recommended

2. **Install Java Development Kit (JDK)**:
   - Android Studio includes JDK, but you can also install separately
   - Required: JDK 8 or later

3. **Android SDK**:
   - Android Studio will prompt to install required SDK components
   - Required: SDK Platform 24 (Android 7.0) through 34

### Step-by-Step Build Instructions

#### 1. Open Project in Android Studio

```bash
# Navigate to the project directory
cd /path/to/app/android

# Open Android Studio and select "Open an existing project"
# Navigate to /app/android directory
```

#### 2. Gradle Sync

When you first open the project:
- Android Studio will detect the Gradle files
- Click "Sync Now" in the notification bar
- Wait for Gradle sync to complete (may take 2-5 minutes on first run)

If sync fails:
```
File → Invalidate Caches / Restart → Invalidate and Restart
```

#### 3. Build the APK

**Option A: Via Android Studio UI**
- Click `Build` menu → `Build Bundle(s) / APK(s)` → `Build APK(s)`
- Wait for build to complete
- Click "locate" in the notification to find the APK

**Option B: Via Gradle Command**
```bash
cd /app/android
./gradlew assembleDebug

# APK location:
# /app/android/app/build/outputs/apk/debug/app-debug.apk
```

#### 4. Install on Device

**Option A: Direct Run from Android Studio**
1. Connect Android device via USB (enable USB Debugging)
2. Or start Android Emulator
3. Click the green "Run" button (▶️)
4. Select your device from the list

**Option B: Manual APK Installation**
```bash
# Using ADB
adb install /app/android/app/build/outputs/apk/debug/app-debug.apk

# Or transfer APK to device and install manually
```

## 🔧 Development Workflow

### Code Structure

**MainActivity.kt** (`/app/android/app/src/main/java/com/safeflow/MainActivity.kt`)
- Handles UI and user interactions
- Checks accessibility permission status
- Manages the Protection Active switch
- Provides button to open accessibility settings

**MyAccessibilityService.kt** (`/app/android/app/src/main/java/com/safeflow/MyAccessibilityService.kt`)
- Core blocking logic
- Monitors app switches via AccessibilityService
- Contains `BLOCKED_PACKAGES` list (line ~13)
- Performs GLOBAL_ACTION_BACK when blocked app detected

### Making Changes

#### Modifying Blocked Apps List

1. Open `MyAccessibilityService.kt`
2. Locate the companion object:
```kotlin
private val BLOCKED_PACKAGES = listOf(
    "com.microsoft.bing"
)
```
3. Add/remove package names
4. Rebuild: `Build → Make Project` (Ctrl+F9)
5. Reinstall on device

#### Changing UI

1. Edit XML layout: `/app/android/app/src/main/res/layout/activity_main.xml`
2. Or modify MainActivity.kt for programmatic changes
3. Preview changes: Open XML file → "Design" or "Split" view
4. Rebuild and run

#### Adding New Features

Example: Add a blocked apps counter

1. Add a TextView to `activity_main.xml`
2. Update `MyAccessibilityService.kt`:
```kotlin
companion object {
    var blockedCount = 0
}

override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    // ... existing code ...
    if (packageName != null && isBlockedPackage(packageName)) {
        blockedCount++
        // ... rest of blocking logic ...
    }
}
```
3. Update `MainActivity.kt` to display the counter

### Testing

#### Logcat Debugging

View logs in Android Studio:
1. Click "Logcat" tab (bottom of window)
2. Filter by "SafeFlow" to see app-specific logs
3. Monitor for:
   - "SafeFlow Accessibility Service Connected"
   - "Blocked app detected: ..."
   - "Back action performed on: ..."

#### Manual Testing Checklist

- [ ] App installs without errors
- [ ] Main screen displays correctly
- [ ] "Open Accessibility Settings" button works
- [ ] Can enable SafeFlow in Accessibility settings
- [ ] Switch turns ON after granting permission
- [ ] Opening Bing (or blocked app) immediately closes it
- [ ] App continues blocking after screen lock/unlock
- [ ] Blocking works after device restart

#### Testing on Emulator

```bash
# Start emulator
emulator -avd Pixel_4_API_30

# Or use Android Studio AVD Manager:
# Tools → AVD Manager → Click "Play" on desired emulator
```

## 📦 Gradle Commands

```bash
# Navigate to android directory
cd /app/android

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Clean + Build
./gradlew clean assembleDebug

# Install on connected device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug

# Run lint checks
./gradlew lint

# Run all checks and build
./gradlew build
```

## 🐛 Common Issues & Solutions

### Issue: "SDK location not found"

**Solution:**
Create `local.properties` file in `/app/android/`:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
# Or on Windows:
# sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

### Issue: "Gradle sync failed"

**Solution 1:** Update Gradle
```bash
./gradlew wrapper --gradle-version=8.0
```

**Solution 2:** Invalidate caches
```
File → Invalidate Caches / Restart → Invalidate and Restart
```

### Issue: "Permission denied" on gradlew

**Solution:**
```bash
chmod +x /app/android/gradlew
```

### Issue: Service not blocking apps

**Solutions:**
1. Check Logcat for errors
2. Verify accessibility permission is granted:
   ```bash
   adb shell settings get secure enabled_accessibility_services
   # Should include: com.safeflow/com.safeflow.MyAccessibilityService
   ```
3. Restart the device
4. Ensure package name is correct (case-sensitive)

### Issue: App crashes on startup

**Solution:**
Check Logcat for stack trace:
```bash
adb logcat | grep -A 20 AndroidRuntime
```

## 🔐 Release Build (Optional)

To create a signed release APK:

1. **Generate Keystore**:
```bash
keytool -genkey -v -keystore safeflow.keystore -alias safeflow -keyalg RSA -keysize 2048 -validity 10000
```

2. **Add to app/build.gradle**:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("path/to/safeflow.keystore")
            storePassword "your_password"
            keyAlias "safeflow"
            keyPassword "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

3. **Build Release**:
```bash
./gradlew assembleRelease
```

## 📱 Device Setup

### Enable USB Debugging

1. Go to Settings → About Phone
2. Tap "Build Number" 7 times to enable Developer Options
3. Go to Settings → Developer Options
4. Enable "USB Debugging"

### Verify Connection

```bash
# Check connected devices
adb devices

# Should show:
# List of devices attached
# ABCD1234    device
```

## 🚀 Continuous Development Tips

1. **Keep Gradle updated**: Regularly update in `build.gradle`
2. **Use Git**: Version control your changes
3. **Test on real device**: Emulators may not perfectly simulate accessibility services
4. **Monitor battery**: Accessibility services can impact battery life
5. **Handle edge cases**: Some apps may restart themselves

## 📚 Additional Resources

- [Android Accessibility Guide](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Studio Guide](https://developer.android.com/studio/intro)
- [Gradle Build Tool](https://docs.gradle.org/)

---

**Need Help?**
- Check Logcat for detailed error messages
- Review Android Studio build output
- Verify all XML files have proper syntax
- Ensure all imports are correct in Kotlin files
