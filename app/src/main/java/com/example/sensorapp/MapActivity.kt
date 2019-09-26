package com.example.sensorapp

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapActivity : AppCompatActivity() {

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
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}