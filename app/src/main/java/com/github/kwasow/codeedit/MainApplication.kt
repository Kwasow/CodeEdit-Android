package com.github.kwasow.codeedit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

// The MainApplication class is used to initialize and prepare thing before the app launches
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create all the necessary notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            var channelName = getString(R.string.no_translation_activeConnectionChannel)
            var notificationChannel = NotificationChannel(
                "activeConnectionChannel", channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    // Notification ids
    // -> 100 - foreground connection service
    //    101-149 - other notifications for that service
}
