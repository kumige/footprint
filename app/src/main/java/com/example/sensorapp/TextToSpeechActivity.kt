package com.example.sensorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TextToSpeechActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texttospeech)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "TTS test"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    // Handles navigating back to main
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}