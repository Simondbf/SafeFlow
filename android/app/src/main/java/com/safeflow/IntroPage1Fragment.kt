package com.safeflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class IntroPage1Fragment : Fragment() {

    interface OnNextClickListener {
        fun onNextClicked()
    }

    private var listener: OnNextClickListener? = null

    fun setOnNextClickListener(listener: OnNextClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro_page1, container, false)
        
        val btnStart = view.findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            listener?.onNextClicked()
        }
        
        return view
    }
}