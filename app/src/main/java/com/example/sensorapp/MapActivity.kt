package com.example.sensorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Map"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        btn_startRun.setOnClickListener { startRun() }
        btn_stopRun.setOnClickListener { stopRun() }

        // Map
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        // Set starting location before tracking
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val startPoint = GeoPoint(task.result!!.latitude, task.result!!.longitude)
                map.controller.setCenter(startPoint)
                setMarker(startPoint)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onReceiveResult(message: Message?) {
        when (message?.what) {
            0 -> {
                val geoPoint = message.obj as GeoPoint
                geoPoints.add(geoPoint)
                if (map.overlays.isNotEmpty()) {
                    //Remove old marker
                    map.overlays.clear()
                    map.invalidate()
                    Log.d("dbg", "marker removed")
                }
                val line = Polyline()
                line.setPoints(geoPoints)
                line.color = getColor(R.color.colorPrimaryDark)
                map.overlayManager.add(line)

                setMarker(geoPoint)
                map.controller.animateTo(geoPoint)
            }
            1 -> {

                val time = message.obj as Int
                textView_time.text = Utils().formatTimer(time, 0)
            }
            2 -> {
                val distance = message.obj as Int
                val dDistance = distance.toDouble()
                val roundedDistance = BigDecimal(dDistance / 1000).setScale(2, RoundingMode.HALF_EVEN)
                textView_distance.text = "$roundedDistance km"
            }
        }

    }

    // Set a marker on given location
    private fun setMarker(geoPoint: GeoPoint) {
        if (requestingLocationUpdates) {
            val marker = Marker(map)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(marker)
        }
    }

    override fun onResume() {
        super.onResume()
        requestingLocationUpdates = true
        //if (checkLocationPermission()) startRun()
    }

    override fun onPause() {
        super.onPause()
        requestingLocationUpdates = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (map.overlays.isNotEmpty()) {
            //Remove old marker
            map.overlays.clear()
            map.invalidate()
        }
        Log.d("dbg", "MapActivity onDestroy")
    }

    private fun startRun() {
        requestingLocationUpdates = true
        btn_startRun.visibility = Button.GONE
        btn_stopRun.visibility = Button.VISIBLE
        val intent = Intent(this, NavigationService::class.java)
        handler = TrackingHandler(this)
        intent.putExtra("handler", Messenger(handler))
        startService(intent)
    }

    private fun stopRun() {
        requestingLocationUpdates = false
        btn_startRun.visibility = Button.VISIBLE
        btn_stopRun.visibility = Button.GONE
        val intent = Intent(this, NavigationService::class.java)
        stopService(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}