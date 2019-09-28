package com.example.sensorapp

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

@SuppressLint("ParcelCreator")
class TrackingHandler(receiver: AppReceiver): Handler(), Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var appReceiver = receiver

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)
        appReceiver.onReceiveResult(msg)
    }

    interface AppReceiver {
        fun onReceiveResult(message: Message?)
    }

}