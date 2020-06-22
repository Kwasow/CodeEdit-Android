package com.material.labs.codeedit.utils

import android.app.AlertDialog
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.material.labs.codeedit.R
import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session

class ConnectionService : Service() {

    private val binder = LocalBinder()

    private lateinit var connection: Connection
    var hostname: String? = null
    var username: String? = null
    private var password: String? = null
    var port: Int = 22

    // TODO: There is no error handling - that should be in the callbacks too
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        hostname = intent?.getStringExtra("hostname")
        username = intent?.getStringExtra("username")
        password = intent?.getStringExtra("password")
        // Keep 22 if intent is null
        port = intent?.getIntExtra("port", 22) ?: 22

        // Stop the service if any important details is missing
        if (hostname.isNullOrEmpty().or(username.isNullOrEmpty())) {
            stopSelf()
        }

        // Connect to remote
        val connectionThread = Thread {
            connection = Connection(hostname, port)
            connection.connect()
            if (password.isNullOrEmpty()) {
                connection.authenticateWithNone(username)
            } else {
                connection.authenticateWithPassword(username, password)
            }

            // Bring service into foreground and post notification
            val notificationText =
                getString(R.string.connected_to) + " " + username + "@" + hostname
            val notificationBuilder =
                NotificationCompat.Builder(this, "activeConnectionChannel")
                .setSmallIcon(R.drawable.ic_file_other)
                .setContentTitle(getString(R.string.connection_active))
                .setContentText(notificationText)
                .setOngoing(true)
            startForeground(100, notificationBuilder.build())
        }

        // Check if we are on a WIFI/Ethernet network
        if ((NetworkState.type(this) == NetworkState.State.WIFI) or
            (NetworkState.type(this) == NetworkState.State.ETHERNET)) {
            connectionThread.start()
        } else {
            val builder = AlertDialog.Builder(this)
            var message = resources.getString(R.string.network_not_wifi)
            message += " " + when (NetworkState.type(this)) {
                NetworkState.State.MOBILE ->
                    resources.getString(R.string.on_mobile)
                NetworkState.State.OFFLINE ->
                    resources.getString(R.string.offline)
                // If error or something else
                else ->
                    resources.getString(R.string.something_wrong)
            }

            builder.setMessage(message)
            builder.setTitle(R.string.alert)
            builder.setPositiveButton(R.string.button_continue) { _: DialogInterface, _: Int ->
                connectionThread.start()
            }
            builder.setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                stopSelf()
            }

            builder.show()
        }

        return START_NOT_STICKY
    }

    fun newSession(): Session {
        return connection.openSession()
    }

    fun disconnect() {
        stopForeground(true)
        stopSelf()
    }

    fun isConnected(): Boolean = this::connection.isInitialized

    // TODO: It needs an interface with callbacks, so that the service updates views when something
    //  happens and not the other way around

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::connection.isInitialized) {
            Thread {
                connection.close()
            }.start()
        }
    }

    // This service's binder
    inner class LocalBinder : Binder() {
        fun getService(): ConnectionService = this@ConnectionService
    }
}