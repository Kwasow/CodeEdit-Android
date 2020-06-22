package com.material.labs.codeedit

import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText

import com.material.labs.codeedit.databinding.ActivityMainBinding
import com.material.labs.codeedit.interfaces.ConnectionCallbacks
import com.material.labs.codeedit.utils.ConnectionService

import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    private lateinit var serviceIntent: Intent

    private lateinit var connectionCallbacks: ConnectionCallbacks
    private var connectionService: ConnectionService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            connectionService?.addCallback(connectionCallbacks)

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

        connectionCallbacks = object : ConnectionCallbacks {
            override fun onConnected() {
                runOnUiThread {
                    // Update buttons
                    layoutBinding.root.mainButtonConnect.isEnabled = false
                    layoutBinding.root.mainButtonLaunchTerminal.isEnabled = true
                    layoutBinding.root.mainButtonLaunchFiles.isEnabled = true
                    layoutBinding.root.mainButtonDisconnect.isEnabled = true
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    // Update buttons
                    layoutBinding.root.mainButtonConnect.isEnabled = true
                    layoutBinding.root.mainButtonLaunchTerminal.isEnabled = false
                    layoutBinding.root.mainButtonLaunchFiles.isEnabled = false
                    layoutBinding.root.mainButtonDisconnect.isEnabled = false
                }
            }


            override fun onError(error: String) {
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Error")
                        .setMessage(error)
                        .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .show()
                }
            }
        }

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
    }

    override fun onDestroy() {
        super.onDestroy()

        connectionService?.removeCallback(connectionCallbacks)

        unbindService(serviceConnection)
    }
}
