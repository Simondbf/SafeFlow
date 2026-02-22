package com.safeflow

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private var currentStep = 0
    private val totalSteps = 3

    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var startButton: Button
    private lateinit var stepIndicator: TextView
    private lateinit var discordButton: Button
    private lateinit var activateProtectionButton: Button
    private lateinit var openAccessibilityButton: Button

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if first run
        val prefs = getSharedPreferences("SafeFlowPrefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        if (!isFirstRun) {
            // Not first run, go directly to MainActivity
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_intro)

        // Initialize device admin
        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        // Initialize views
        titleText = findViewById(R.id.introTitle)
        descriptionText = findViewById(R.id.introDescription)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        startButton = findViewById(R.id.startButton)
        stepIndicator = findViewById(R.id.stepIndicator)
        discordButton = findViewById(R.id.discordButton)
        activateProtectionButton = findViewById(R.id.activateProtectionButton)
        openAccessibilityButton = findViewById(R.id.openAccessibilityButton)

        // Set up buttons
        nextButton.setOnClickListener {
            if (currentStep < totalSteps - 1) {
                currentStep++
                updateContent()
            }
        }
        
        previousButton.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                updateContent()
            }
        }

        startButton.setOnClickListener {
            // Check if Device Admin is activated
            if (!devicePolicyManager.isAdminActive(adminComponent)) {
                Toast.makeText(
                    this,
                    "⚠️ Veuillez activer la protection d'abord",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            
            // Mark as not first run
            prefs.edit().putBoolean("isFirstRun", false).apply()
            startMainActivity()
        }

        discordButton.setOnClickListener {
            openDiscord()
        }

        activateProtectionButton.setOnClickListener {
            activateDeviceAdmin()
        }
        
        openAccessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

        // Show first step
        updateContent()
    }

    private fun updateContent() {
        when (currentStep) {
            0 -> {
                // Step 1: Welcome
                titleText.text = "Bienvenue sur SafeFlow"
                descriptionText.text = "SafeFlow protège votre navigation en bloquant automatiquement les sites indésirables.\n\nRestez concentré et en sécurité."
                nextButton.visibility = View.VISIBLE
                startButton.visibility = View.GONE
                discordButton.visibility = View.GONE
                activateProtectionButton.visibility = View.GONE
                stepIndicator.text = "1 / 3"
            }
            1 -> {
                // Step 2: Permissions
                titleText.text = "Permissions Requises"
                descriptionText.text = "SafeFlow a besoin du service d'accessibilité pour détecter et bloquer les applications.\n\nVos données restent privées et locales."
                nextButton.visibility = View.VISIBLE
                startButton.visibility = View.GONE
                discordButton.visibility = View.GONE
                activateProtectionButton.visibility = View.GONE
                stepIndicator.text = "2 / 3"
            }
            2 -> {
                // Step 3: Security & Community
                titleText.text = "Protection Maximale"
                descriptionText.text = "Activez la protection anti-désinstallation pour sécuriser l'application.\n\nRejoignez aussi notre communauté Discord !"
                nextButton.visibility = View.GONE
                startButton.visibility = View.VISIBLE
                discordButton.visibility = View.VISIBLE
                activateProtectionButton.visibility = View.VISIBLE
                stepIndicator.text = "3 / 3"
            }
        }
    }

    private fun activateDeviceAdmin() {
        try {
            if (!devicePolicyManager.isAdminActive(adminComponent)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "SafeFlow a besoin de cette permission pour empêcher la désinstallation non autorisée."
                )
                startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
            } else {
                Toast.makeText(this, "Protection déjà activée", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de l'activation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Protection Anti-Désinstallation Activée ✓", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Protection non activée (optionnel)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDiscord() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/safeflow"))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
