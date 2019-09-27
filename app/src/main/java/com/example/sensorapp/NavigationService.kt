package com.example.sensorapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.PowerManager
import android.telephony.ServiceState
import android.util.Log


class NavigationService : Service() {


    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(9999, startForegroundService())

    }

    private fun startForegroundService(): Notification? {
        Log.d("dbg", "location permission granted")

        val channel = NotificationChannel(
            "channel_01",
            "Navigation channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "test notification"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        val builder = Notification.Builder(applicationContext, "channel_01")
        return builder.build()
    }
}



