package com.safeflow

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class IntroPage3Fragment : Fragment() {

    interface OnNextClickListener {
        fun onNextClicked()
    }

    private var listener: OnNextClickListener? = null
    private lateinit var btnNext: Button
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1001
    }

    fun setOnNextClickListener(listener: OnNextClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro_page3, container, false)
        
        devicePolicyManager = requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(requireContext(), AdminReceiver::class.java)
        
        val btnActivate = view.findViewById<Button>(R.id.btnActivate)
        btnNext = view.findViewById(R.id.btnNext)
        
        btnActivate.setOnClickListener {
            activateDeviceAdmin()
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
            val isEnabled = devicePolicyManager.isAdminActive(adminComponent)
            btnNext.isEnabled = isEnabled
            btnNext.alpha = if (isEnabled) 1.0f else 0.5f
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
                Toast.makeText(requireContext(), "Protection déjà activée", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Erreur lors de l'activation", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Protection Activée ✓", Toast.LENGTH_LONG).show()
                updateNextButtonState()
            } else {
                Toast.makeText(requireContext(), "Protection non activée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}