package com.example.sensorapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_addname.*
import org.jetbrains.anko.doAsync


class NameAddingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addname)

        btn_addName.setOnClickListener{ addNane()}
    }

    fun addNane(){
        var name = editText_name.text.toString()
        name = name.trim()
        Log.d("asd","$name")
        if(!name.isBlank()){
            Log.d("asd","$name")
            val db = Room.databaseBuilder(
                this,
                AppDatabase::class.java, "user.db"
            ).build()
            doAsync {
                db.dao().insertName(User(0, name))
                goMainActivity()
            }
        } else {
            name_errorMsg.text = " Name can not be empty"
            name_errorMsg.visibility = View.VISIBLE
        }
    }

    fun goMainActivity(){
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}