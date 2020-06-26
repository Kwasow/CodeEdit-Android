package com.material.labs.codeedit

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.material.labs.codeedit.adapters.ServersAdapter

import com.material.labs.codeedit.databinding.ActivityMainBinding
import com.material.labs.codeedit.interfaces.ConnectionCallbacks
import com.material.labs.codeedit.utils.ConnectionService
import com.material.labs.codeedit.utils.RemoteInfoManager

class MainActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityMainBinding

    private lateinit var serviceIntent: Intent

    private lateinit var connectionCallbacks: ConnectionCallbacks
    private var connectionService: ConnectionService? = null
    private var isBound = false

    private lateinit var servers: MutableList<RemoteInfoManager>
    private var current: String? = null
    private var connected: Boolean? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ConnectionService.LocalBinder
            connectionService = binder.getService()
            isBound = true

            connectionService?.addCallback(connectionCallbacks)

            if (connectionService?.isConnected() == true) {
                current = connectionService?.currentHostname()
                updateServerList(current)
            } else {
                updateServerList(null)
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
                    // Re-binding seems to be the easiest option to update connection details
                    // though it should be improved, since it's not a clean solution
                    if (isBound) {
                        unbindService(serviceConnection)
                    }

                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }

            override fun onDisconnected() {
                runOnUiThread {
                    // Re-binding seems to be the easiest option to update connection details
                    // though it should be improved, since it's not a clean solution
                    if (isBound) {
                        unbindService(serviceConnection)
                    }

                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    // Re-binding seems to be the easiest option to update connection details
                    // though it should be improved, since it's not a clean solution
                    if (isBound) {
                        unbindService(serviceConnection)
                    }

                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }
        }

        // Connect to service and update server list
        serviceIntent = Intent(this, ConnectionService::class.java)
        servers = RemoteInfoManager.get(this)

        setContentView(layoutBinding.root)
    }

    override fun onResume() {
        super.onResume()

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()

        connectionService?.removeCallback(connectionCallbacks)

        if (isBound) {
            unbindService(serviceConnection)
        }
    }

    private fun updateServerList(connected: String?) {
        // Check if there are any servers saved
        if (servers.isEmpty()) {
            layoutBinding.noServersText.visibility = View.VISIBLE
            layoutBinding.serverListRecyclerView.visibility = View.GONE
        } else {
            layoutBinding.serverListRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = ServersAdapter(servers, connected)
                visibility = View.VISIBLE
            }
            layoutBinding.noServersText.visibility = View.GONE
        }
    }

    fun addServer(v: View) {
        val intent = Intent(this, ServerAddActivity::class.java)
        startActivity(intent)
    }
}
