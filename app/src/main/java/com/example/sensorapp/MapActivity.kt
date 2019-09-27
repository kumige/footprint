package com.example.sensorapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
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

const val PERMISSON_CODE = 124
const val PRIORITY_HIGH_ACCURACY = 100

class MapActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Map"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Map
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)

        // Current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                val startPoint = GeoPoint(task.result!!.latitude, task.result!!.longitude)
                map.controller.setCenter(startPoint)
                Log.d(
                    "GEOLOCATION",
                    "latitude: ${task.result?.latitude} and longitude: ${task.result?.longitude}"
                )
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (requestingLocationUpdates) {
                    for (location in locationResult.locations) {
                        // Update UI with location data
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        Log.d("dbg", "$geoPoint")

                        if (map.overlays.isNotEmpty()) {
                            //Remove old marker
                            map.overlays.clear()
                            map.invalidate()
                            Log.d("dbg", "marker removed")
                        }

                        //Create and set new marker on updated location
                        val marker = Marker(map)
                        marker.position = geoPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        map.overlays.add(marker)
                        Log.d("dbg", "marker added")
                        map.controller.animateTo(geoPoint)
                    }
                }


            }
        }

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.priority = PRIORITY_HIGH_ACCURACY
    }

    override fun onResume() {
        super.onResume()
        requestingLocationUpdates = true
        if (requestingLocationUpdates) startLocationUpdates()
        //if (checkLocationPermission()) startNavService()
    }

    override fun onPause() {
        super.onPause()
        requestingLocationUpdates = false
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startNavService() {

    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSON_CODE
                )

            }
        } else {
            // Location permission granted
            return true
        }
        // No location permission
        return false
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}