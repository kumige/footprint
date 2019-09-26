package com.example.sensorapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_openMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            // start your next activity
            startActivity(intent)
        }


        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "user.db"
        ).build()

        doAsync {
            Log.d("dbg", "${LocalDateTime.now()}")
            db.dao().insertRun(History(0, "${LocalDateTime.now()}", 100, 5000))
            val data = db.dao().getAllHistory()

            Log.d("dbg", data.toString())

        }


    }
}
