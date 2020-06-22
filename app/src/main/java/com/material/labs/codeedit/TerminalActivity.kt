package com.material.labs.codeedit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.material.labs.codeedit.databinding.ActivityTerminalBinding
import com.material.labs.codeedit.views.TerminalView

class TerminalActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityTerminalBinding
    private lateinit var terminalView: TerminalView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityTerminalBinding.inflate(layoutInflater)
        terminalView = layoutBinding.terminalView
        terminalView.open()

        setContentView(layoutBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()

        terminalView.onDestroy()
    }
}