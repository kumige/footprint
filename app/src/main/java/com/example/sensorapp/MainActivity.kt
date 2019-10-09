package com.example.sensorapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.sensorapp.fragments.HistoryFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    private lateinit var fragment: HistoryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Checks if user has already set a name
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "user.db"
        ).build()
        doAsync {
            var user = db.dao().getUsername()
            var users = db.dao().getUser()
            Log.d("asd","users: $users")
            //db.dao().deleteUserName(User(16, "d"))
            Log.d("asd","$user")
            if(user == null){
                addName()
            }
        }

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

    // Opens an activity where user sets the name for the first time
    fun addName(){
        val intent = Intent(this, NameAddingActivity::class.java)
        startActivity(intent)
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
