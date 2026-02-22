package com.safeflow

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val updateChecker by lazy { UpdateChecker(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            // Find views
            val enableButton = findViewById<Button>(R.id.enableProtectionButton)
            val versionText = findViewById<TextView>(R.id.versionText)
            
            // Set version text
            versionText?.text = "v1.0"
            
            // Set up enable protection button
            enableButton?.setOnClickListener {
                try {
                    openAccessibilitySettings()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Check for updates
            checkForUpdates()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkForUpdates() {
        try {
            updateChecker.checkForUpdate { updateInfo ->
                if (updateInfo != null) {
                    // Run on UI thread to show dialog
                    runOnUiThread {
                        showUpdateDialog(updateInfo)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUpdateDialog(updateInfo: UpdateChecker.UpdateInfo) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Mise à jour disponible")
                .setMessage("Une nouvelle version (${updateInfo.version}) est prête. Voulez-vous la télécharger ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    updateChecker.downloadUpdate(updateInfo.downloadUrl)
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
