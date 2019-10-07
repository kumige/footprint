package com.example.sensorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sensorapp.fragments.HistoryFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var fragment: HistoryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileLayout.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        cardView_run.setOnClickListener {
            if (Utils().checkLocationPermission(applicationContext)) {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            }
            else {
                Toast.makeText(
                    applicationContext,
                    "You must give access to your location for this app to work correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        loadFragment()
    }

    private fun loadFragment() {
        val fManager = supportFragmentManager
        val fTransaction = fManager.beginTransaction()
        fragment = HistoryFragment()
        fTransaction.replace(R.id.historyContainer, fragment)
        fTransaction.commit()
    }

    // Ask location permission
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
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
        }
    }
}
