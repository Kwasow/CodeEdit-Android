package com.github.kwasow.codeedit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.kwasow.codeedit.databinding.ActivityServerDetailsBinding
import com.github.kwasow.codeedit.interfaces.ConnectionCallbacks
import com.github.kwasow.codeedit.utils.ConnectionService
import com.github.kwasow.codeedit.utils.RemoteInfoManager

class ServerDetailsActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityServerDetailsBinding

    private lateinit var serviceIntent: Intent

    private lateinit var details: RemoteInfoManager
    private var hostname: String? = null
    private var username: String? = null
    private var port: Int? = null

    private lateinit var connectionCallbacks: ConnectionCallbacks
    private var connectionService: ConnectionService? = null
    private var isBound = false

    @SuppressLint("SetTextI18n")
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            connectionService?.addCallback(connectionCallbacks)

            // If service is already connected to something
            if (connectionService?.isConnected() == true &&
                connectionService?.currentHostname() == "$username@$hostname:$port") {
                // Update buttons
                layoutBinding.mainButtonConnect.isEnabled = false
                layoutBinding.mainButtonLaunchTerminal.isEnabled = true
                layoutBinding.mainButtonLaunchFiles.isEnabled = true
                layoutBinding.mainButtonDisconnect.isEnabled = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionService = null
            isBound = false
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        details = intent.getSerializableExtra("details") as RemoteInfoManager
        hostname = details.hostname
        username = details.username
        port = details.port

        layoutBinding = ActivityServerDetailsBinding.inflate(layoutInflater)

        serviceIntent = Intent(this, ConnectionService::class.java)

        layoutBinding.serverName.text = details.alias
        layoutBinding.serverUsernameAddress.text =
            " $username@$hostname"
        layoutBinding.serverOS.text = " ${details.os}"

        connectionCallbacks = object : ConnectionCallbacks {
            override fun onConnected() {
                runOnUiThread {
                    layoutBinding.mainButtonConnect.isEnabled = false
                    layoutBinding.mainButtonLaunchTerminal.isEnabled = true
                    layoutBinding.mainButtonLaunchFiles.isEnabled = true
                    layoutBinding.mainButtonDisconnect.isEnabled = true
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    layoutBinding.mainButtonConnect.isEnabled = true
                    layoutBinding.mainButtonLaunchTerminal.isEnabled = false
                    layoutBinding.mainButtonLaunchFiles.isEnabled = false
                    layoutBinding.mainButtonDisconnect.isEnabled = false
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    AlertDialog.Builder(this@ServerDetailsActivity)
                        .setTitle("Error")
                        .setMessage(error)
                        .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .show()
                }
            }

            override fun onServerOSUpdated(newOS: String) {
                super.onServerOSUpdated(newOS)

                runOnUiThread {
                    layoutBinding.serverOS.text = " $newOS"
                }
            }
        }

        serviceIntent = Intent(this, ConnectionService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContentView(layoutBinding.root)
    }

    fun connect(v: View) {
        val alert = Dialog(this)
        alert.setContentView(R.layout.dialog_password)
        val buttonConnect = alert.findViewById<Button>(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            serviceIntent.putExtra("details", details)
            serviceIntent.putExtra(
                "password",
                alert.findViewById<EditText>(R.id.inputPassword).text.toString())

            if (!isBound) {
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }

            // Start the service
            startService(serviceIntent)
            alert.dismiss()
        }
        val buttonCancel = alert.findViewById<Button>(R.id.buttonCancel)
        buttonCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()

    }

    // TODO: These two launch things have to be replaced with one single launchIDE
    fun launchTerminal(v: View) {
        val intent = Intent(this, TerminalActivity::class.java)
        startActivity(intent)
    }

    fun launchFiles(v: View) {
        val intent = Intent(this, FilesActivity::class.java)
        startActivity(intent)
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

    fun delete(v: View) {
        if (!isBound) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        // Stop the service if we're deleting the currently active connection
        if (connectionService?.isConnected() == true
            && connectionService?.currentHostname() == "$username@$hostname:$port") {
            connectionService?.disconnect()
        }

        // Delete and finish this activity
        details.delete(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        connectionService?.removeCallback(connectionCallbacks)

        unbindService(serviceConnection)
    }
}