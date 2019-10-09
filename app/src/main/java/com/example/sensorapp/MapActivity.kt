package com.example.sensorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

const val PERMISSON_CODE = 124
const val PRIORITY_HIGH_ACCURACY = 100

class MapActivity : AppCompatActivity(), TrackingHandler.AppReceiver {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var handler: TrackingHandler
    private var requestingLocationUpdates = false
    private var geoPoints = mutableListOf<GeoPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (App.isNightModeEnabled()) {
            setTheme(R.style.DarkTheme)
        } else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        btn_startRun.setOnClickListener { startRun() }
        btn_stopRun.setOnClickListener { stopRun() }

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    @SuppressLint("SetTextI18n")
    override fun onReceiveResult(message: Message?) {
        when (message?.what) {
            LOCATION_UPDATE -> {
                val geoPoint = message.obj as GeoPoint
                geoPoints.add(geoPoint)
                if (map.overlays.isNotEmpty()) {
                    //Remove old marker
                    map.overlays.clear()
                    map.invalidate()
                }
                val line = Polyline()
                line.setPoints(geoPoints)
                line.color = getColor(R.color.colorPrimaryDark)
                map.overlayManager.add(line)

                setMarker(geoPoint)
                map.controller.animateTo(geoPoint)
            }
            TIMER_UPDATE -> {

                val time = message.obj as Int
                textView_time.text = Utils().formatTimer(time, FORMAT_TIMER_CLOCK)
            }
            DISTANCE_UPDATE -> {
                val distance = message.obj as Int
                val dDistance = distance.toDouble()
                val roundedDistance =
                    BigDecimal(dDistance / 1000).setScale(2, RoundingMode.HALF_EVEN)
                textView_distance.text = "$roundedDistance km"
            }
        }

    }

    // Set a marker on given location
    private fun setMarker(geoPoint: GeoPoint) {
            val marker = Marker(map)
            marker.icon = getDrawable(R.drawable.location_marker)
            marker.position = geoPoint
            marker.setAnchor(0.2.toFloat(), 0.2.toFloat())
            map.overlays.clear()
            map.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()

        // Set initial location
        if (!requestingLocationUpdates) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if (location != null) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        map.controller.setCenter(geoPoint)
                        map.overlay.clear()
                        setMarker(geoPoint)
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (map.overlays.isNotEmpty()) {
            //Remove old marker
            map.overlays.clear()
            map.invalidate()
        }
    }

    // Start NavigationService
    private fun startRun() {
        requestingLocationUpdates = true
        btn_startRun.visibility = Button.GONE
        btn_stopRun.visibility = Button.VISIBLE
        val intent = Intent(this, NavigationService::class.java)
        handler = TrackingHandler(this)
        intent.putExtra("handler", Messenger(handler))
        startService(intent)
    }

    // Stop NavigationService
    private fun stopRun() {
        requestingLocationUpdates = false
        btn_startRun.visibility = Button.VISIBLE
        btn_stopRun.visibility = Button.GONE
        val intent = Intent(this, NavigationService::class.java)
        stopService(intent)
        onBackPressed()
    }

    override fun onBackPressed() {
        if (requestingLocationUpdates) {
            this.moveTaskToBack(true)
        } else super.onBackPressed()


    }
}