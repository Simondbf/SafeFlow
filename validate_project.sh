#!/bin/bash

# SafeFlow Project Validation Script
# This script checks if all required files are in place

echo "🔍 SafeFlow Project Validation"
echo "=============================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
total_checks=0
passed_checks=0
failed_checks=0

# Function to check file existence
check_file() {
    total_checks=$((total_checks + 1))
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        passed_checks=$((passed_checks + 1))
        return 0
    else
        echo -e "${RED}✗${NC} $2 - MISSING: $1"
        failed_checks=$((failed_checks + 1))
        return 1
    fi
}

# Function to check directory existence
check_dir() {
    total_checks=$((total_checks + 1))
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        passed_checks=$((passed_checks + 1))
        return 0
    else
        echo -e "${RED}✗${NC} $2 - MISSING: $1"
        failed_checks=$((failed_checks + 1))
        return 1
    fi
}

echo "📚 Documentation Files:"
check_file "README.md" "README.md"
check_file "BUILD_GUIDE.md" "BUILD_GUIDE.md"
check_file "QUICK_START.md" "QUICK_START.md"
check_file "PROJECT_OVERVIEW.md" "PROJECT_OVERVIEW.md"
echo ""

echo "📱 Android Project Structure:"
check_dir "android" "Android root directory"
check_file "android/build.gradle" "Project-level build.gradle"
check_file "android/settings.gradle" "settings.gradle"
check_file "android/gradle.properties" "gradle.properties"
echo ""

echo "🏗️ App Module:"
check_file "android/app/build.gradle" "App-level build.gradle"
check_file "android/app/proguard-rules.pro" "ProGuard rules"
echo ""

echo "📄 Android Manifest & Config:"
check_file "android/app/src/main/AndroidManifest.xml" "AndroidManifest.xml"
check_file "android/app/src/main/res/xml/accessibility_service_config.xml" "Accessibility service config"
echo ""

echo "💻 Kotlin Source Files:"
check_file "android/app/src/main/java/com/safeflow/MainActivity.kt" "MainActivity.kt"
check_file "android/app/src/main/java/com/safeflow/MyAccessibilityService.kt" "MyAccessibilityService.kt"
echo ""

echo "🎨 UI Resources:"
check_file "android/app/src/main/res/layout/activity_main.xml" "activity_main.xml layout"
check_file "android/app/src/main/res/values/strings.xml" "strings.xml"
check_file "android/app/src/main/res/values/colors.xml" "colors.xml"
echo ""

echo "=============================="
echo "📊 VALIDATION SUMMARY"
echo "=============================="
echo -e "Total Checks: ${total_checks}"
echo -e "${GREEN}Passed: ${passed_checks}${NC}"
echo -e "${RED}Failed: ${failed_checks}${NC}"
echo ""

if [ $failed_checks -eq 0 ]; then
    echo -e "${GREEN}✅ All files are in place! Project is ready to build.${NC}"
    echo ""
    echo "🚀 Next Steps:"
    echo "1. Open Android Studio"
    echo "2. Open the /android directory"
    echo "3. Wait for Gradle sync"
    echo "4. Click the green Run button"
    echo ""
    exit 0
else
    echo -e "${RED}❌ Some files are missing. Please check the errors above.${NC}"
    echo ""
    exit 1
fi
