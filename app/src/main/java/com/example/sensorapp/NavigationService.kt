package com.example.sensorapp

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.google.android.gms.location.*
import org.jetbrains.anko.doAsync
import org.osmdroid.util.Distance
import org.osmdroid.util.GeoPoint
import org.threeten.bp.LocalDateTime
import java.lang.Exception
import kotlin.math.sqrt
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlin.math.cos
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlin.math.asin
import kotlin.math.sin
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


const val CHANNEL_ID = "channel_01"
const val LOCATION_UPDATE = 0
const val TIMER_UPDATE = 1
const val DISTANCE_UPDATE = 2

class NavigationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var messenger: Messenger? = null
    private var trackedPoints = mutableListOf<GeoPoint>()
    private lateinit var previousPoint: GeoPoint
    private var totalDistance = 0.0
    private lateinit var startTime: LocalDateTime
    private lateinit var endTime: LocalDateTime
    private var runTime = 0
    private lateinit var timerThread: Thread
    private var timerIsRunning = false

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

        startTime = LocalDateTime.now()
        startTimer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    Log.d("dbg", "$geoPoint")

                    if (::previousPoint.isInitialized) {
                        val locA = Location("")
                        locA.latitude = geoPoint.latitude
                        locA.longitude = geoPoint.longitude

                        val locB = Location("")
                        locB.latitude = previousPoint.latitude
                        locB.longitude = previousPoint.longitude

                        totalDistance += locA.distanceTo(locB)
                    }
                    previousPoint = geoPoint

                    // Send location update to UI thread
                    trackedPoints.add(geoPoint)
                    val msg = Message()
                    msg.obj = geoPoint
                    msg.what = LOCATION_UPDATE
                    messenger?.send(msg)

                    val distanceMsg = Message()
                    distanceMsg.obj = totalDistance.toInt()
                    distanceMsg.what = DISTANCE_UPDATE
                    messenger?.send(distanceMsg)
                }
            }
        }

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.priority = PRIORITY_HIGH_ACCURACY

        // Start requesting location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Starts the timer in a new thread
    private fun startTimer() {
        timerThread = Thread {
            Looper.prepare()
            timerIsRunning = true
            while (timerIsRunning) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    Thread.currentThread().interrupt()
                    Log.d("dbg", "$e")
                }
                if (timerIsRunning) {
                    runTime++
                    val msg = Message()
                    msg.obj = runTime
                    msg.what = TIMER_UPDATE
                    messenger?.send(msg)
                }
            }
        }
        timerThread.start()
    }

    // Service killed, run ends here and it is added to database
    override fun onDestroy() {
        super.onDestroy()

        timerIsRunning = false
        fusedLocationClient.removeLocationUpdates(locationCallback)

        Log.d("dbg", "${trackedPoints[0]}")
        Log.d("dbg", "$trackedPoints")

        /*var totalDistance = 0.0
        var next = 1
        for (geoPoint in trackedPoints) {
            if (geoPoint != trackedPoints.last()) {
                val nextPoint = trackedPoints[next]

                val locA = Location("")
                locA.latitude = geoPoint.latitude
                locA.longitude = geoPoint.longitude

                val locB = Location("")
                locB.latitude = nextPoint.latitude
                locB.longitude = nextPoint.longitude

                totalDistance += locA.distanceTo(locB)
                next++
            }
        }
        */

        Log.d("dbg", "trackedPoints size: ${trackedPoints.size}")
        Log.d("dbg", "totalDistance: $totalDistance")


        val distance = if (totalDistance > 1.0) {
            totalDistance.toInt()
        } else {
            1
        }

        endTime = LocalDateTime.now()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "user.db"
        ).build()

        // Convert tracked route to json string
        val route = DbTypeConverters().geoPointListToString(trackedPoints)
        doAsync {
            db.dao().insertRun(History(0, "$startTime", "$endTime", runTime, distance, route))
            runTime = 0
        }
        Log.d("dbg", "nav service ondestroy")
    }

    private fun createNotificationChannel() {
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


