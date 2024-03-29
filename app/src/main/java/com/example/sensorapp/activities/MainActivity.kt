package com.example.sensorapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.sensorapp.*
import com.example.sensorapp.fragments.HistoryFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    private lateinit var fragment: HistoryFragment
    private lateinit var data: MutableList<History>

    override fun onCreate(savedInstanceState: Bundle?) {
        if (App.isNightModeEnabled()) {
            setTheme(R.style.DarkTheme)
        } else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Checks if user has already set a name
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "user.db"
        ).build()
        doAsync {
            val user = db.dao().getUsername()
            val users = db.dao().getUser()
            Log.d("asd", "users: $users")
            //db.dao().deleteUserName(User(16, "d"))
            Log.d("asd", "user: $user")
            if (user == null) {
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
            } else {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.main_noLocationAccessToast),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        checkPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (App.isNightModeEnabled()) {
            menuInflater.inflate(R.menu.icon_theme_light, menu)
        } else menuInflater.inflate(R.menu.icon_theme_dark, menu)
        return true
    }

    // Opens an activity where user sets the name for the first time
    private fun addName() {
        val intent = Intent(this, NameAddingActivity::class.java)
        startActivity(intent)
    }

    fun toggleTheme(item: MenuItem): Boolean {
        App.setIsNightModeEnabled(!App.isNightModeEnabled())
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
        return true
    }

    override fun onResume() {
        super.onResume()
        Log.d("dbg", "mainactivity onResume")
        loadFragment()
    }

    private fun loadFragment() {
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "user.db"
        ).build()

        val fManager = supportFragmentManager
        val fTransaction = fManager.beginTransaction()
        fragment = HistoryFragment()

        doAsync {
            data = db.dao().getAllHistory()

            uiThread {
                if (data.size > 0) {
                    fTransaction.replace(R.id.fragLayout, fragment)
                    fTransaction.commit()
                    emptyHistoryLayout.visibility = View.GONE
                    fragLayout.visibility = View.VISIBLE
                } else {
                    emptyHistoryLayout.visibility = View.VISIBLE
                    fragLayout.visibility = View.GONE
                }
            }
        }


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
