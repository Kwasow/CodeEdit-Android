package com.material.labs.codeedit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.View

import com.material.labs.codeedit.databinding.ActivityMainBinding

// TODO: In the future there should be a service making hold of the connection.
// Other classes and views will only connect to it and ask for new sessions

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(layoutBinding.root)
    }

    fun launchTerminal(v: View) {
        val intent = Intent(this, TerminalActivity::class.java)
        startActivity(intent)
    }

    fun launchFiles(v: View) {
        val intent = Intent(this, FilesActivity::class.java)
        startActivity(intent)
    }
}
