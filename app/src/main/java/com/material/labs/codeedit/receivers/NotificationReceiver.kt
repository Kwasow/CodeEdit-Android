package com.material.labs.codeedit.receivers

import android.content.*
import com.material.labs.codeedit.utils.CodeLogger
import com.material.labs.codeedit.utils.ConnectionService

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val SERVER_DISCONNECT = "com.material.labs.codeedit.SERVER_DISCONNECT"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (val action = intent?.action) {
            SERVER_DISCONNECT -> {
                val serviceIntent = Intent(context, ConnectionService::class.java)
                val binder = peekService(context, serviceIntent) as ConnectionService.LocalBinder
                val service = binder.getService()
                service.disconnect()
            }
            else -> CodeLogger.logE("Action $action is not supported")
        }
    }
}