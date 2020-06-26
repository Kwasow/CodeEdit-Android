package com.github.kwasow.codeedit.utils

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.Service
import android.content.DialogInterface
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.github.kwasow.codeedit.R
import com.github.kwasow.codeedit.interfaces.ConnectionCallbacks
import com.github.kwasow.codeedit.receivers.NotificationReceiver

import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session
import java.io.IOException

// This is a service that keeps a connection to the remote service alive and
class ConnectionService : Service() {

    private val binder = LocalBinder()

    private lateinit var connection: Connection
    private lateinit var details: RemoteInfoManager
    var hostname: String? = null
    var username: String? = null
    private var password: String? = null
    var port: Int = 22

    private val connectionCallbacks = mutableListOf<ConnectionCallbacks>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        details = intent!!.getSerializableExtra("details") as RemoteInfoManager
        hostname = details.hostname
        username = details.username
        password = intent.getStringExtra("password")
        // Keep 22 if it is null for some reason
        port = details.port

        // Stop the service if any important details is missing
        if (hostname.isNullOrEmpty().or(username.isNullOrEmpty())) {
            stopSelf()
        }

        // Connect to remote
        val connectionThread = Thread {
            try {
                connection = Connection(hostname, port)
                connection.connect()
                if (password.isNullOrEmpty()) {
                    connection.authenticateWithNone(username)
                } else {
                    connection.authenticateWithPassword(username, password)
                }

                // Bring service into foreground and post notification
                val disconnectIntent =
                    Intent(this, NotificationReceiver::class.java).apply {
                        action = NotificationReceiver.SERVER_DISCONNECT
                    }
                val disconnectPendingIntent =
                    PendingIntent.getBroadcast(this, 0, disconnectIntent, 0)
                val notificationText =
                    getString(R.string.connected_to) + " " + username + "@" + hostname
                val notificationBuilder =
                    NotificationCompat.Builder(this, "activeConnectionChannel")
                        .setSmallIcon(R.drawable.ic_file_other)
                        .setContentTitle(getString(R.string.connection_active))
                        .setContentText(notificationText)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_file_other, "Disconnect", disconnectPendingIntent)
                startForeground(100, notificationBuilder.build())

                checkOS()

                connectionCallbacks.forEach {
                    it.onConnected()
                }
            } catch (e: IOException) {
                CodeLogger.logE(e)
                connectionCallbacks.forEach {
                    it.onError(e.toString())
                }
                stopSelf()
            }
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
        connectionCallbacks.forEach {
            it.onDisconnected()
        }

        stopForeground(true)
        stopSelf()
        // stopSelf() does not destroy the service
        onDestroy()
    }

    fun isConnected(): Boolean = this::connection.isInitialized

    fun currentHostname(): String = "$username@$hostname:$port"

    fun addCallback(callback: ConnectionCallbacks) {
        connectionCallbacks.add(callback)
    }

    fun removeCallback(callback: ConnectionCallbacks) {
        connectionCallbacks.remove(callback)
    }

    // TODO: This is fine for checking, but the UI should update the OS as well without the need
    //  to restart the app
    private fun checkOS() {
        // If hasn't been checked or wasn't recognized by a previous version
        if (details.os == "Not checked"
            || details.os == "unknown") {

            val session = connection.openSession()
            val stdout = session.stdout
            session.execCommand("uname")

            Thread {
                var returnString = ""

                var x = stdout?.read()
                while (x != -1) {
                    returnString += x?.toChar()
                    x = stdout?.read()
                }

                // Close session after read is done
                session?.close()

                val os: String = when (returnString.removeSuffix("\n")) {
                    "Linux" -> "Linux"
                    "Darwin" -> "macOS"
                    else -> "unknown"
                }

                val newDetails = RemoteInfoManager(
                    details.alias,
                    details.hostname,
                    details.username,
                    os,
                    details.port
                )
                details.update(newDetails, this)
            }.start()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        connectionCallbacks.forEach {
            it.onDisconnected()
        }

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