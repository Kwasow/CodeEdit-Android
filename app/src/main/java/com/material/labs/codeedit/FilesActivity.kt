package com.material.labs.codeedit

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.material.labs.codeedit.databinding.ActivityFilesBinding
import com.material.labs.codeedit.views.FilesView

class FilesActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityFilesBinding
    private lateinit var files: FilesView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityFilesBinding.inflate(layoutInflater)
        files = layoutBinding.filesView

        // This is a temporary solution for the testing stage
        val alert = Dialog(this)
        alert.setContentView(R.layout.dialog_connect)
        val buttonConnect = alert.findViewById<Button>(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            if (alert.findViewById<EditText>(R.id.inputPort).text.toString().isNotEmpty()) {
                files.connect(
                    alert.findViewById<EditText>(R.id.inputHostname).text.toString(),
                    alert.findViewById<EditText>(R.id.inputUsername).text.toString(),
                    alert.findViewById<EditText>(R.id.inputPassword).text.toString(),
                    alert.findViewById<EditText>(R.id.inputPort).text.toString().toInt()
                )
            } else {
                files.connect(
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

    // TODO: Fix window leaking
    override fun onDestroy() {
        super.onDestroy()

        files.onDestroy()
    }

}