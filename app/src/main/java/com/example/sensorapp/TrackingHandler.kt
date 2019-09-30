package com.example.sensorapp

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class TrackingHandler(receiver: AppReceiver): Handler() {

    private var appReceiver = receiver

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        appReceiver.onReceiveResult(msg)
    }

    interface AppReceiver {
        fun onReceiveResult(message: Message?)
    }

}