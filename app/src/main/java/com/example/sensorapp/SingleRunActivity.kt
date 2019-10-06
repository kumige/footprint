package com.example.sensorapp

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_single_run.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.math.BigDecimal
import java.math.RoundingMode

class SingleRunActivity : AppCompatActivity() {

    private lateinit var historyItem: History
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_run)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "History"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Map
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = findViewById(R.id.map)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)

        historyItem = Utils().stringToHistoryObject(intent.getStringExtra("RUN"))
        val date = historyItem.startTime.split("T")
        val time = date[1].slice(0..4)
        val km = historyItem.distance.toDouble() / 1000
        val distanceInKm = BigDecimal(km).setScale(2, RoundingMode.HALF_EVEN)
        Log.d("dbg", "distance: $distanceInKm")
        val minutes = historyItem.duration.toDouble() / 60.0
        val pace = BigDecimal(minutes / km).setScale(2, RoundingMode.HALF_EVEN)

        textView_runCompletedAt.text = "Run completed on ${date[0]} $time"
        textView_timeAndDistance.text = "$distanceInKm km in ${Utils().formatTimer(historyItem.duration, 0)}"
        textView_pace.text = "$pace min/km"

        val geoPoints = DbTypeConverters().stringToGeoPointList(historyItem.route)
        val line = Polyline()
        line.setPoints(geoPoints)
        line.color = getColor(R.color.colorPrimaryDark)
        map.overlayManager.add(line)
        if (geoPoints.isNotEmpty()) {
            val startPoint = geoPoints[0]
            map.controller.animateTo(startPoint)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}