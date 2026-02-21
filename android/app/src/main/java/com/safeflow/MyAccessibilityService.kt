package com.safeflow

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SafeFlow"
        
        // BLOCKED PACKAGES LIST - Easy to modify for future features
        // Example: Add "Allowed Browser" logic by removing the user's chosen browser from this list
        private val BLOCKED_PACKAGES = listOf(
            "com.microsoft.bing"  // Bing Search (for testing)
            // Add more packages here as needed:
            // "com.android.chrome",
            // "com.instagram.android"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "SafeFlow Accessibility Service Connected")
        Log.d(TAG, "Blocking packages: $BLOCKED_PACKAGES")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Only process window state changed events
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            
            if (packageName != null && isBlockedPackage(packageName)) {
                Log.d(TAG, "Blocked app detected: $packageName - Closing...")
                
                // Perform back action to close the app
                performGlobalAction(GLOBAL_ACTION_BACK)
                
                Log.d(TAG, "Back action performed on: $packageName")
            }
        }
    }

    private fun isBlockedPackage(packageName: String): Boolean {
        return BLOCKED_PACKAGES.any { blockedPackage ->
            packageName.equals(blockedPackage, ignoreCase = true)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "SafeFlow Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SafeFlow Accessibility Service Destroyed")
    }
}
