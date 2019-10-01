package com.example.sensorapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_texttospeech.*
import java.util.*
import android.speech.tts.Voice
import kotlin.collections.HashSet


class TextToSpeechActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texttospeech)

        tts = TextToSpeech(this,this, "com.google.android.tts")

        btn_useTTS.setOnClickListener{ speakOut() }

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "TTS test"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            var voiceGender = HashSet<String>()
            voiceGender.add("female")
            //Finnish TTS voice
            var newVoice = Voice("fi-FI-language", Locale("fi", "FI"), 400,200,false, voiceGender)

            // English TTS voice
            //var newVoice = Voice("en-US-language", Locale("en", "US"), 400,200,false, voiceGender)
            //tts.setVoice(newVoice)
            tts.setSpeechRate(0.8f)

            val result = tts.setVoice(newVoice)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                btn_useTTS!!.isEnabled = true
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }

    }

    private fun speakOut() {

        // Lists all the voices available
        /*for (tmpVoice in tts.voices) {
        Log.d("voices","${tmpVoice}")
        }*/
        
        tts!!.speak("Tämä on testi", TextToSpeech.QUEUE_FLUSH, null, "")
    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    // Handles navigating back to main
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}