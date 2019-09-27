package com.example.sensorapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Dao
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime

class ProfileActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var history: List<History>

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: HistoryRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        loadProfileData()

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

    }

    private fun loadProfileData() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "user.db"
        ).build()

        doAsync {
            //db.dao().insertRun(History(0, "${LocalDateTime.now()}", 344, 1337))

            name = db.dao().getUsername()
            history = db.dao().getAllHistory()

            textView_username.text = name
            adapter = HistoryRecyclerAdapter(history)
            recyclerView.adapter = adapter
        }
    }
}