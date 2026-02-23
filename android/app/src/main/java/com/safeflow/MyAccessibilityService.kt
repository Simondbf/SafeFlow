package com.safeflow

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SafeFlow"
        
        // Browser packages to monitor
        private val BROWSER_PACKAGES = listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.brave.browser",
            "com.sec.android.app.sbrowser"
        )
    }

    private lateinit var blockedSitesManager: BlockedSitesManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            blockedSitesManager = BlockedSitesManager(this)
            Log.d(TAG, "SafeFlow Service Connected")
            Log.d(TAG, "Monitoring keywords: ${blockedSitesManager.getBlockedKeywords()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onServiceConnected: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null) return

            // Listen to TYPE_WINDOW_CONTENT_CHANGED for faster detection
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                val packageName = event.packageName?.toString()
                if (packageName.isNullOrEmpty()) return
                
                // Check if it's a browser
                if (isBrowser(packageName)) {
                    checkAndBlockContent(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onAccessibilityEvent: ${e.message}")
        }
    }

    private fun isBrowser(packageName: String): Boolean {
        return BROWSER_PACKAGES.any { browser ->
            packageName.equals(browser, ignoreCase = true)
        }
    }

    private fun checkAndBlockContent(event: AccessibilityEvent) {
        try {
            val source = event.source ?: rootInActiveWindow
            if (source == null) return
            
            // Extract URL or content
            val contentText = extractContent(source)
            source.recycle()
            
            if (contentText.isNullOrEmpty()) return
            
            // Check if content contains blocked keywords
            if (blockedSitesManager.isUrlBlocked(contentText)) {
                Log.d(TAG, "Blocked content detected: $contentText")
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d(TAG, "Back action performed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking content: ${e.message}")
        }
    }

    private fun extractContent(node: AccessibilityNodeInfo?): String? {
        try {
            if (node == null) return null
            
            val className = node.className?.toString() ?: ""
            
            // Check EditText (address bar)
            if (className.contains("EditText", ignoreCase = true)) {
                val text = node.text?.toString()
                if (!text.isNullOrEmpty()) {
                    return text
                }
            }
            
            // Check content description
            val contentDesc = node.contentDescription?.toString()
            if (!contentDesc.isNullOrEmpty()) {
                return contentDesc
            }
            
            // Recursively check children
            for (i in 0 until node.childCount) {
                try {
                    val child = node.getChild(i)
                    val content = extractContent(child)
                    child?.recycle()
                    if (!content.isNullOrEmpty()) {
                        return content
                    }
                } catch (e: Exception) {
                    // Skip problematic child
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting content: ${e.message}")
            return null
        }
    }

    override fun onInterrupt() {
        try {
            Log.d(TAG, "SafeFlow Service Interrupted")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onInterrupt: ${e.message}")
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            Log.d(TAG, "SafeFlow Service Destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
    }
}
