package com.safeflow

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class IntroPage2Fragment : Fragment() {

    interface OnNextClickListener {
        fun onNextClicked()
    }

    private var listener: OnNextClickListener? = null
    private lateinit var btnNext: Button

    fun setOnNextClickListener(listener: OnNextClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro_page2, container, false)
        
        val btnOpenSettings = view.findViewById<Button>(R.id.btnOpenSettings)
        btnNext = view.findViewById(R.id.btnNext)
        
        btnOpenSettings.setOnClickListener {
            openAccessibilitySettings()
        }
        
        btnNext.setOnClickListener {
            listener?.onNextClicked()
        }
        
        // Initially disable next button
        updateNextButtonState()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        updateNextButtonState()
    }

    private fun updateNextButtonState() {
        if (::btnNext.isInitialized) {
            val isEnabled = isAccessibilityServiceEnabled()
            btnNext.isEnabled = isEnabled
            btnNext.alpha = if (isEnabled) 1.0f else 0.5f
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val service = "${requireContext().packageName}/${MyAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                requireContext().contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(service) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}