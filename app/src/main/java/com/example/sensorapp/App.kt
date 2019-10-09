package com.example.sensorapp

import android.app.Application
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log


class App: Application() {

    companion object {
        private var isNightModeEnabled = false
        private lateinit var mPrefs: SharedPreferences

        fun isNightModeEnabled(): Boolean {
            return isNightModeEnabled
        }

        fun setIsNightModeEnabled(isNightModeEnabled: Boolean) {
            this.isNightModeEnabled = isNightModeEnabled
            mPrefs.edit().putBoolean("NIGHT_MODE", isNightModeEnabled).apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        isNightModeEnabled = mPrefs.getBoolean("NIGHT_MODE", false)
        Log.d("dbg", "nightmode: $isNightModeEnabled")
    }



}