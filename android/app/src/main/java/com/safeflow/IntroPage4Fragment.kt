package com.safeflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class IntroPage4Fragment : Fragment() {

    interface OnFinishClickListener {
        fun onFinishClicked()
    }

    private var listener: OnFinishClickListener? = null

    fun setOnFinishClickListener(listener: OnFinishClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro_page4, container, false)
        
        val btnDiscord = view.findViewById<Button>(R.id.btnDiscord)
        val btnFinish = view.findViewById<Button>(R.id.btnFinish)
        
        btnDiscord.setOnClickListener {
            openDiscord()
        }
        
        btnFinish.setOnClickListener {
            listener?.onFinishClicked()
        }
        
        return view
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