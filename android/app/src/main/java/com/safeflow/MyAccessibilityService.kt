package com.safeflow

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SafeFlow"
        
        // BLOCKED PACKAGES (Apps)
        private val BLOCKED_PACKAGES = listOf(
            "com.microsoft.bing"
        )
        
        // BLOCKED DOMAINS (Websites)
        private val BLOCKED_DOMAINS = listOf(
            "bing.com",
            "manga-news.com"
        )
        
        // BROWSER PACKAGES to monitor for URL blocking
        private val BROWSER_PACKAGES = listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.brave.browser",
            "com.sec.android.app.sbrowser"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            Log.d(TAG, "SafeFlow Accessibility Service Connected")
            Log.d(TAG, "Blocking packages: $BLOCKED_PACKAGES")
            Log.d(TAG, "Blocking domains: $BLOCKED_DOMAINS")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onServiceConnected: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null) return

            // Only process window state changed events
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString()
                
                if (packageName.isNullOrEmpty()) return
                
                // Check if app package is blocked
                if (isBlockedPackage(packageName)) {
                    Log.d(TAG, "Blocked app detected: $packageName - Closing...")
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d(TAG, "Back action performed on: $packageName")
                    return
                }
                
                // Check if it's a browser - then check URL
                if (isBrowser(packageName)) {
                    checkAndBlockUrl(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onAccessibilityEvent: ${e.message}")
        }
    }

    private fun isBlockedPackage(packageName: String): Boolean {
        return try {
            BLOCKED_PACKAGES.any { blockedPackage ->
                packageName.equals(blockedPackage, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking blocked package: ${e.message}")
            false
        }
    }

    private fun isBrowser(packageName: String): Boolean {
        return BROWSER_PACKAGES.any { browser ->
            packageName.equals(browser, ignoreCase = true)
        }
    }

    private fun checkAndBlockUrl(event: AccessibilityEvent) {
        try {
            val source = event.source ?: rootInActiveWindow
            if (source == null) return
            
            val url = extractUrl(source)
            source.recycle()
            
            if (url.isNullOrEmpty()) return
            
            Log.d(TAG, "Detected URL: $url")
            
            // Check if URL contains blocked domain
            if (isBlockedUrl(url)) {
                Log.d(TAG, "Blocked domain detected in URL: $url - Closing...")
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d(TAG, "Back action performed on blocked URL")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking URL: ${e.message}")
        }
    }

    private fun extractUrl(node: AccessibilityNodeInfo?): String? {
        try {
            if (node == null) return null
            
            // Try to find URL in various common locations
            val className = node.className?.toString() ?: ""
            
            // Check if it's an EditText (address bar)
            if (className.contains("EditText", ignoreCase = true)) {
                val text = node.text?.toString()
                if (!text.isNullOrEmpty() && (text.contains("http") || text.contains("."))) {
                    return text
                }
            }
            
            // Recursively search children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val url = extractUrl(child)
                child?.recycle()
                if (!url.isNullOrEmpty()) {
                    return url
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting URL: ${e.message}")
            return null
        }
    }

    private fun isBlockedUrl(url: String): Boolean {
        return try {
            val lowerUrl = url.lowercase()
            BLOCKED_DOMAINS.any { domain ->
                lowerUrl.contains(domain.lowercase())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking blocked URL: ${e.message}")
            false
        }
    }

    override fun onInterrupt() {
        try {
            Log.d(TAG, "SafeFlow Accessibility Service Interrupted")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onInterrupt: ${e.message}")
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            Log.d(TAG, "SafeFlow Accessibility Service Destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
    }
}
