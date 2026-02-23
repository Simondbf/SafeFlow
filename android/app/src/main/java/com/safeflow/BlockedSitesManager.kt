package com.safeflow

import android.content.Context
import android.content.SharedPreferences

class BlockedSitesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "SafeFlowBlockedSites"
        private const val KEY_BLOCKED_SITES = "blockedSites"
        
        // Default blocked sites
        private val DEFAULT_BLOCKED_KEYWORDS = listOf(
            "bing",
            "manga"
        )
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get all blocked keywords (default + user added)
     */
    fun getBlockedKeywords(): List<String> {
        val userSites = getUserAddedSites()
        return DEFAULT_BLOCKED_KEYWORDS + userSites
    }

    /**
     * Get only user-added sites
     */
    fun getUserAddedSites(): List<String> {
        val sitesString = prefs.getString(KEY_BLOCKED_SITES, "") ?: ""
        return if (sitesString.isEmpty()) {
            emptyList()
        } else {
            sitesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    /**
     * Add a new site to block
     */
    fun addBlockedSite(site: String): Boolean {
        if (site.trim().isEmpty()) return false
        
        val currentSites = getUserAddedSites().toMutableList()
        val cleanSite = site.trim().lowercase()
        
        // Check if already exists
        if (currentSites.contains(cleanSite) || DEFAULT_BLOCKED_KEYWORDS.contains(cleanSite)) {
            return false
        }
        
        currentSites.add(cleanSite)
        saveSites(currentSites)
        return true
    }

    /**
     * Remove a blocked site
     */
    fun removeBlockedSite(site: String): Boolean {
        val currentSites = getUserAddedSites().toMutableList()
        val removed = currentSites.remove(site.trim().lowercase())
        if (removed) {
            saveSites(currentSites)
        }
        return removed
    }

    /**
     * Clear all user-added sites (keep defaults)
     */
    fun clearUserSites() {
        prefs.edit().remove(KEY_BLOCKED_SITES).apply()
    }

    /**
     * Check if a URL contains any blocked keyword
     */
    fun isUrlBlocked(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return getBlockedKeywords().any { keyword ->
            lowerUrl.contains(keyword.lowercase())
        }
    }

    private fun saveSites(sites: List<String>) {
        val sitesString = sites.joinToString(",")
        prefs.edit().putString(KEY_BLOCKED_SITES, sitesString).apply()
    }
}
