package com.material.labs.codeedit

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle

import com.material.labs.codeedit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)

        val actionBar = supportActionBar
        actionBar?.title = "Servers"
        actionBar?.hide()

        var terminalView = layoutBinding.terminalView
        terminalView.connect("192.168.0.10")
/*
        layoutBinding.addServerFab.setOnClickListener {
            terminalView.close()
        }
*/
        setContentView(layoutBinding.root)
    }
}
