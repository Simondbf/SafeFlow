package com.safeflow

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var protectionSwitch: SwitchCompat
    private lateinit var switchLabel: TextView
    private lateinit var infoText: TextView
    private lateinit var openSettingsButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        protectionSwitch = findViewById(R.id.protectionSwitch)
        switchLabel = findViewById(R.id.switchLabel)
        infoText = findViewById(R.id.infoText)
        openSettingsButton = findViewById(R.id.openSettingsButton)

        // Check if accessibility service is enabled
        checkAccessibilityPermission()

        // Set up switch listener
        protectionSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (isAccessibilityServiceEnabled()) {
                    // Service is enabled, update UI
                    switchLabel.text = getString(R.string.protection_active)
                    switchLabel.setTextColor(getColor(R.color.success))
                } else {
                    // Service not enabled, prompt user
                    protectionSwitch.isChecked = false
                    showAccessibilityPrompt()
                }
            } else {
                switchLabel.text = getString(R.string.protection_inactive)
                switchLabel.setTextColor(getColor(R.color.text_secondary))
            }
        }

        // Open accessibility settings button
        openSettingsButton.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission when returning to the app
        checkAccessibilityPermission()
    }

    private fun checkAccessibilityPermission() {
        if (isAccessibilityServiceEnabled()) {
            // Service is enabled
            infoText.visibility = View.GONE
            openSettingsButton.visibility = View.GONE
            protectionSwitch.isEnabled = true
            protectionSwitch.isChecked = true
            switchLabel.text = getString(R.string.protection_active)
            switchLabel.setTextColor(getColor(R.color.success))
        } else {
            // Service is not enabled
            showAccessibilityPrompt()
        }
    }

    private fun showAccessibilityPrompt() {
        infoText.visibility = View.VISIBLE
        openSettingsButton.visibility = View.VISIBLE
        protectionSwitch.isEnabled = false
        switchLabel.text = getString(R.string.protection_inactive)
        switchLabel.setTextColor(getColor(R.color.text_secondary))
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${MyAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
