package com.safeflow

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateChecker(private val context: Context) {

    companion object {
        private const val TAG = "UpdateChecker"
        private const val CURRENT_VERSION = "1.0"
        private const val UPDATE_URL = "https://raw.githubusercontent.com/Simondbf/SafeFlow/main/version.json"
    }

    data class UpdateInfo(
        val version: String,
        val downloadUrl: String,
        val releaseNotes: String
    )

    fun checkForUpdate(callback: (UpdateInfo?) -> Unit) {
        Thread {
            try {
                val url = URL(UPDATE_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val latestVersion = json.getString("version")
                    val downloadUrl = json.getString("download_url")
                    val releaseNotes = json.optString("release_notes", "Nouvelle version disponible")

                    Log.d(TAG, "Current version: $CURRENT_VERSION, Latest version: $latestVersion")

                    if (isNewerVersion(CURRENT_VERSION, latestVersion)) {
                        callback(UpdateInfo(latestVersion, downloadUrl, releaseNotes))
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e(TAG, "Failed to check for updates: ${connection.responseCode}")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates: ${e.message}")
                callback(null)
            }
        }.start()
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        return try {
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val currentPart = currentParts.getOrElse(i) { 0 }
                val latestPart = latestParts.getOrElse(i) { 0 }

                if (latestPart > currentPart) return true
                if (latestPart < currentPart) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun downloadUpdate(downloadUrl: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
            request.setTitle("SafeFlow Update")
            request.setDescription("Téléchargement de la mise à jour...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SafeFlow-update.apk")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Log.d(TAG, "Update download started")
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update: ${e.message}")
        }
    }
}
