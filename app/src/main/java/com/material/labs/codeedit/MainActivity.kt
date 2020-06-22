package com.material.labs.codeedit

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText

import com.material.labs.codeedit.databinding.ActivityMainBinding
import com.material.labs.codeedit.utils.ConnectionService
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    private lateinit var serviceIntent: Intent

    private var connectionService: ConnectionService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            // If service is already connected to something
            if (connectionService?.isConnected() == true) {
                // Update buttons
                layoutBinding.root.mainButtonConnect.isEnabled = false
                layoutBinding.root.mainButtonLaunchTerminal.isEnabled = true
                layoutBinding.root.mainButtonLaunchFiles.isEnabled = true
                layoutBinding.root.mainButtonDisconnect.isEnabled = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutBinding = ActivityMainBinding.inflate(layoutInflater)

        serviceIntent = Intent(this, ConnectionService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

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

    fun connect(v: View) {
        val alert = Dialog(this)
        alert.setContentView(R.layout.dialog_connect)
        val buttonConnect = alert.findViewById<Button>(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            println(alert.findViewById<EditText>(R.id.inputHostname).text.toString())
            serviceIntent.putExtra(
                "hostname",
                alert.findViewById<EditText>(R.id.inputHostname).text.toString())
            serviceIntent.putExtra(
                "username",
                alert.findViewById<EditText>(R.id.inputUsername).text.toString())
            serviceIntent.putExtra(
                "password",
                alert.findViewById<EditText>(R.id.inputPassword).text.toString())
            serviceIntent.putExtra(
                "port",
                alert.findViewById<EditText>(R.id.inputPort).text.toString().toInt())

            if (!isBound) {
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }

            startService(serviceIntent)
            alert.dismiss()

            // Update buttons
            layoutBinding.root.mainButtonConnect.isEnabled = false
            layoutBinding.root.mainButtonLaunchTerminal.isEnabled = true
            layoutBinding.root.mainButtonLaunchFiles.isEnabled = true
            layoutBinding.root.mainButtonDisconnect.isEnabled = true
        }
        val buttonCancel = alert.findViewById<Button>(R.id.buttonCancel)
        buttonCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    fun disconnect(v: View) {
        // Stop service
        if (!isBound) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        connectionService?.disconnect()

        // Reset intent
        serviceIntent = Intent(this, ConnectionService::class.java)

        // Update buttons
        layoutBinding.root.mainButtonConnect.isEnabled = true
        layoutBinding.root.mainButtonLaunchTerminal.isEnabled = false
        layoutBinding.root.mainButtonLaunchFiles.isEnabled = false
        layoutBinding.root.mainButtonDisconnect.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(serviceConnection)
    }
}
