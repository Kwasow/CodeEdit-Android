package com.material.labs.codeedit

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.material.labs.codeedit.databinding.ActivityTerminalBinding

class TerminalActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityTerminalBinding.inflate(layoutInflater)
        val terminal = layoutBinding.terminalView

        val alert = Dialog(this)
        alert.setContentView(R.layout.dialog_connect)
        val buttonConnect = alert.findViewById<Button>(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            if (alert.findViewById<EditText>(R.id.inputPort).text.toString().isNotEmpty()) {
                terminal.connect(
                    alert.findViewById<EditText>(R.id.inputHostname).text.toString(),
                    alert.findViewById<EditText>(R.id.inputUsername).text.toString(),
                    alert.findViewById<EditText>(R.id.inputPassword).text.toString(),
                    alert.findViewById<EditText>(R.id.inputPort).text.toString().toInt()
                )
            } else {
                terminal.connect(
                    alert.findViewById<EditText>(R.id.inputHostname).text.toString(),
                    alert.findViewById<EditText>(R.id.inputUsername).text.toString(),
                    alert.findViewById<EditText>(R.id.inputPassword).text.toString()
                )
            }

            alert.hide()
            setContentView(layoutBinding.root)
        }
        val buttonCancel = alert.findViewById<Button>(R.id.buttonCancel)
        buttonCancel.setOnClickListener {
            alert.hide()
            finish()
        }
        alert.show()
    }
}