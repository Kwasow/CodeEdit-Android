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
import com.hierynomus.smbj.connection.Connection as SMBConnection
import com.hierynomus.smbj.session.Session as SMBSession
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session
import java.io.IOException

// This is a service that keeps a connection to the remote service alive and
class ConnectionService : Service() {

    private val binder = LocalBinder()

    private lateinit var connection: Connection
    private lateinit var sambaClient: SMBClient
    private lateinit var sambaConnection: SMBConnection
    private lateinit var sambaSession: SMBSession
    private lateinit var details: RemoteInfoManager
    var hostname: String? = null
    var username: String? = null
    private var password: String? = null
    var sshPort: Int = 22
    var sambaPort: Int = 445

    private val connectionCallbacks = mutableListOf<ConnectionCallbacks>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        details = intent!!.getSerializableExtra("details") as RemoteInfoManager
        hostname = details.hostname
        username = details.username
        password = intent.getStringExtra("password")
        // Ports
        sshPort = details.sshPort
        sambaPort = details.sambaPort

        // Stop the service if any important details is missing
        if (hostname.isNullOrEmpty().or(username.isNullOrEmpty())) {
            stopSelf()
        }

        // Connect to remote ssh
        val connectionThread = Thread {
            try {
                connection = Connection(hostname, sshPort)
                connection.connect()
                // TODO: Replace with "keyboard-interactive" in the future
                val connected = if (password.isNullOrEmpty()) {
                    connection.authenticateWithNone(username)
                } else {
                    connection.authenticateWithPassword(username, password)
                }

                if (!connected) {
                    throw IOException("There was a problem connecting to $hostname:$sshPort")
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
                hostname = null; username = null
                onDestroy()
            }
        }

        // Connect to remote samba
        val sambaConnectionThread = Thread {
            sambaClient = SMBClient()

            try {
                val authenticationContext =  if (password.isNullOrEmpty()) {
                    AuthenticationContext.guest()
                } else {
                    AuthenticationContext(username, password!!.toCharArray(), hostname)
                }

                sambaConnection = sambaClient.connect(hostname, sambaPort)
                sambaSession = sambaConnection.authenticate(authenticationContext)

                val diskShare : DiskShare = sambaSession.connectShare("code") as DiskShare

                println(diskShare.list("/"))
            } catch (e: Exception) {
                CodeLogger.logE(e)
            }
        }

        // Check if we are on a WIFI/Ethernet network
        if ((NetworkState.type(this) == NetworkState.State.WIFI) or
            (NetworkState.type(this) == NetworkState.State.ETHERNET)
        ) {
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
            builder.setPositiveButton(R.string.text_continue) { _: DialogInterface, _: Int ->
                connectionThread.start()
            }
            builder.setNegativeButton(R.string.cancel) { _: DialogInterface, _: Int ->
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

    fun currentHostname(): String = "$username@$hostname:$sshPort"

    fun addCallback(callback: ConnectionCallbacks) {
        connectionCallbacks.add(callback)
    }

    fun removeCallback(callback: ConnectionCallbacks) {
        connectionCallbacks.remove(callback)
    }

    private fun checkOS() {
        // If hasn't been checked or wasn't recognized by a previous version
        if (details.os == "Not checked" ||
            details.os == "unknown"
        ) {

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
                    details.sshPort,
                    details.sambaPort,
                    os,
                )

                // Update saved details
                details.update(newDetails, this)

                // Update this service's details
                details = newDetails

                connectionCallbacks.forEach {
                    it.onServerOSUpdated(details.os)
                }
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
