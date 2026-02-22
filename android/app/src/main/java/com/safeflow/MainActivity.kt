package com.safeflow

import android.app.admin.DevicePolicyManager
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val updateChecker by lazy { UpdateChecker(this) }
    private val securityManager by lazy { SecurityManager(this) }
    
    private lateinit var statusIcon: TextView
    private lateinit var statusText: TextView
    private lateinit var statusDescription: TextView
    private lateinit var activateButton: Button
    private lateinit var discordButton: Button
    private lateinit var versionText: TextView
    private lateinit var unlockIcon: TextView
    private lateinit var settingsIcon: TextView

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            // Initialize device admin
            devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            adminComponent = ComponentName(this, AdminReceiver::class.java)
            
            // Initialize security manager and generate code if needed
            securityManager.getUnlockCode()
            
            // Initialize views
            statusIcon = findViewById(R.id.statusIcon)
            statusText = findViewById(R.id.statusText)
            statusDescription = findViewById(R.id.statusDescription)
            activateButton = findViewById(R.id.activateButton)
            discordButton = findViewById(R.id.discordButton)
            versionText = findViewById(R.id.versionText)
            unlockIcon = findViewById(R.id.unlockIcon)
            settingsIcon = findViewById(R.id.settingsIcon)
            
            // Set version
            versionText.text = "v1.1"
            
            // Set up buttons
            activateButton.setOnClickListener {
                openAccessibilitySettings()
            }
            
            discordButton.setOnClickListener {
                openDiscord()
            }

            unlockIcon.setOnClickListener {
                showUnlockDialog()
            }
            
            // Check for updates
            checkForUpdates()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Update protection status when returning to app
        updateProtectionStatus()
    }

    private fun updateProtectionStatus() {
        try {
            val isProtected = isAccessibilityServiceEnabled()
            
            if (isProtected) {
                // Protected state
                statusIcon.text = "🛡️"
                statusText.text = "PROTÉGÉ"
                statusText.setTextColor(getColor(R.color.success))
                statusDescription.text = "SafeFlow est actif et vous protège"
                statusDescription.setTextColor(getColor(R.color.success))
                activateButton.text = "Protection Active"
                activateButton.isEnabled = false
                activateButton.alpha = 0.6f
            } else {
                // Not protected state
                statusIcon.text = "⚠️"
                statusText.text = "NON PROTÉGÉ"
                statusText.setTextColor(getColor(R.color.warning_red))
                statusDescription.text = "Activez la protection pour commencer"
                statusDescription.setTextColor(getColor(R.color.text_secondary))
                activateButton.text = "ACTIVER LA PROTECTION"
                activateButton.isEnabled = true
                activateButton.alpha = 1.0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showUnlockDialog() {
        try {
            val unlockCode = securityManager.getUnlockCode()
            val masterKey = securityManager.getMasterKey()

            // Create input field
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            input.hint = "Entrez le code"

            // Create debug text
            val debugText = TextView(this)
            debugText.text = "DEBUG: $unlockCode\nMASTER: $masterKey"
            debugText.textSize = 10f
            debugText.setTextColor(getColor(R.color.text_secondary))
            debugText.setPadding(16, 8, 16, 8)

            // Create container layout
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 20, 50, 20)
            layout.addView(input)
            layout.addView(debugText)

            AlertDialog.Builder(this)
                .setTitle("🔓 Déverrouillage Protection")
                .setMessage("Entrez le code de déverrouillage pour permettre la désinstallation.")
                .setView(layout)
                .setPositiveButton("Valider") { dialog, _ ->
                    val enteredCode = input.text.toString().trim()
                    if (securityManager.validateCode(enteredCode)) {
                        // Code correct - remove device admin
                        if (devicePolicyManager.isAdminActive(adminComponent)) {
                            devicePolicyManager.removeActiveAdmin(adminComponent)
                            Toast.makeText(
                                this,
                                "✓ Protection Désactivée - Désinstallation possible",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Protection déjà désactivée",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Code incorrect
                        Toast.makeText(this, "❌ Code Incorrect", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Annuler") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors du déverrouillage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val service = "${packageName}/${MyAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(service) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun checkForUpdates() {
        try {
            updateChecker.checkForUpdate { updateInfo ->
                if (updateInfo != null) {
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

    private fun openDiscord() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/safeflow"))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
