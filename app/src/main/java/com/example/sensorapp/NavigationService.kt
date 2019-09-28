package com.example.sensorapp

import android.Manifest
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.preference.PreferenceManager
import android.telephony.ServiceState
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

const val CHANNEL_ID = "channel_01"

class NavigationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var messenger: Messenger? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startTracking()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        messenger = intent?.getParcelableExtra("handler")
        return START_NOT_STICKY
    }

    private fun startTracking() {
        val notificationIntent = Intent(this, MapActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking location")
            .setContentText("run faster")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        startForeground(1, notification)


        // Current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Log.d("dbg", "requesting location")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                    for (location in locationResult.locations) {
                        // Update UI with location data
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        Log.d("dbg", "$geoPoint")

                        val msg = Message()
                        msg.obj = geoPoint
                        msg.what = 0
                        messenger?.send(msg)
                    }
            }
        }

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.priority = PRIORITY_HIGH_ACCURACY

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("dbg", "nav service ondestroy")
    }

    private fun createNotificationChannel() {
        Log.d("dbg", "location permission granted")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navigation channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }

}


