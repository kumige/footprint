package com.example.sensorapp.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_weather.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.*
import android.location.Geocoder
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.sensorapp.App
import com.example.sensorapp.BuildConfig
import com.example.sensorapp.R
import com.example.sensorapp.Utils


class WeatherFragment : Fragment() {

    val apiKey: String = BuildConfig.ApiKey
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    lateinit var progressbar: ProgressBar
    lateinit var linearLayoutMain: LinearLayout
    lateinit var errorMsg: TextView
    lateinit var errorIcon: ImageView
    lateinit var thiscontext: Context
    lateinit var weatherRefresh: ImageView
    lateinit var weatherBackground: ImageView

    lateinit var cityName: String
    var nightMode: Boolean = App.isNightModeEnabled()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        thiscontext = activity!!.applicationContext
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        progressbar = view.findViewById(R.id.loader)
        linearLayoutMain = view.findViewById(R.id.mainLinearLayout)
        errorMsg = view.findViewById(R.id.textView_weatherErrorMsg)
        errorIcon = view.findViewById(R.id.errorImg)
        weatherBackground = view.findViewById(R.id.weather_background)
        weatherRefresh = view.findViewById(R.id.weather_refresh)
        weatherRefresh.setOnClickListener {
            weatherRefresh.startAnimation(AnimationUtils.loadAnimation(thiscontext,
                R.anim.anim
            ))
            refreshWeather() }
        locationCheck()
        return view
    }

    override fun onResume(){
        super.onResume()
        refreshWeather()
    }

    // Checks if location permissions have been given
    private fun locationCheck(){
            if (Utils().checkLocationPermission(activity!!.applicationContext)) getLocation()
            else {
                Toast.makeText(
                    activity!!.applicationContext,
                    getString(R.string.weather_noLocationErrorToast),
                    Toast.LENGTH_LONG
                ).show()
                errorLayout()
                errorMsg.text = getString(R.string.weather_noLocationError)
            }
        }

    // Refreshes weather
    private fun refreshWeather() {
        if (Utils().checkLocationPermission(activity!!.applicationContext)) {
            getLocation()
        }
    }


    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        doAsync {
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                latitude = task.result?.latitude
                longitude = task.result?.longitude

                // Gets current city
                val geocoder = Geocoder(thiscontext, Locale.getDefault())
                var addresses = listOf<Any>()
                addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)

                cityName = addresses.get(0).getLocality()

                weatherTask().execute()
            }
        }
    }

    inner class weatherTask : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {

            // Shows only progress bar and hides error/weather layouts
            super.onPreExecute()
            if (::progressbar.isInitialized && ::linearLayoutMain.isInitialized && ::errorMsg.isInitialized && ::errorIcon.isInitialized && ::weatherBackground.isInitialized) {
                progressbar.visibility = View.VISIBLE
                linearLayoutMain.visibility = View.GONE
                errorMsg.visibility = View.GONE
                errorIcon.visibility = View.GONE
                weatherBackground.visibility = View.GONE
                weatherRefresh.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            }
        }

        // Connects to openweathermap's API
        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey").readText(
                        Charsets.UTF_8
                    )
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        // Handles all data from the openweathermap's API
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val weatherDescription = weather.getString("description")
                val weatherSimple = weather.getString("main")

                // Rounding temperature
                val tempINT = main.getString("temp")
                val tempRounded = BigDecimal(tempINT).setScale(0, RoundingMode.HALF_EVEN).toString()
                val tempString = "$tempRounded°C"

                // Sets temperature, city and background
                textView_temperature.text = tempString
                textView_weatherLocation.text = "$cityName"
                weatherColors(weatherSimple, weatherDescription)

                // Sets the layout visible and hides progressbar
                progressbar.visibility = View.GONE
                linearLayoutMain.visibility = View.VISIBLE
                weatherBackground.visibility = View.VISIBLE
                weatherRefresh.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

                // Gets the correct weather name from R.string
                var newWeatherDescription = weatherDescription
                newWeatherDescription = newWeatherDescription.replace("\\s".toRegex(), "_")
                newWeatherDescription = newWeatherDescription.replace("/", "")
                newWeatherDescription = newWeatherDescription.toLowerCase()
                var string = getStringWithResKey(newWeatherDescription)
                textView_weather.text = string
            } catch (e: Exception) {
                errorLayout()
            }
        }
    }

    // Gets the correct R.string
    private fun getStringWithResKey(nameResKey: String): String {
        var packageName = thiscontext.getPackageName()
        val resId = resources.getIdentifier(nameResKey, "string", packageName)
        return try {
            getString(resId)
        } catch (e: Exception) {
            errorLayout()
            errorMsg.text = getString(R.string.weather_invalidWeatherError)
            Log.e("weather", "Couldn't find string value for key '$nameResKey'", e)
            ""
        }
    }

    // Gets the correct weather background and weather image
    private fun weatherColors(simpleWeather: String, weatherDesc: String) {
        when (simpleWeather) {
            "Thunderstorm" -> {
                weather_icon.setImageResource(R.drawable.thunder)
                weather_background.setBackgroundResource(R.drawable.weather_background_thunder)
            }

            "Drizzle" -> {
                weather_icon.setImageResource(R.drawable.rain2)
                weather_background.setBackgroundResource(R.drawable.weather_background_rain)
            }

            "Rain" -> {
                weather_icon.setImageResource(R.drawable.rain2)
                weather_background.setBackgroundResource(R.drawable.weather_background_rain)
            }

            "Snow" -> {
                weather_icon.setImageResource(R.drawable.snow)
                weather_background.setBackgroundResource(R.drawable.weather_background_snow)
            }

            "Clear" -> {
                weather_icon.setImageResource(R.drawable.sunny)
                weather_background.setBackgroundResource(R.drawable.weather_background_sunny)
            }

            "Clouds" -> {
                if (weatherDesc == "few clouds") {
                    weather_icon.setImageResource(R.drawable.sun_n_cloud)
                    weather_background.setBackgroundResource(R.drawable.weather_background_sun_n_cloud)
                } else {
                    weather_icon.setImageResource(R.drawable.cloudy)
                    weather_background.setBackgroundResource(R.drawable.weather_background_cloudy)
                }
            }

            "Mist" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Smoke" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Haze" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Dust" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Fog" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Sand" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Dust" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Ash" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Squall" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            "Tornado" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
            }

            else -> {
                errorLayout()
                errorMsg.text = getString(R.string.weather_invalidWeatherError)
            }
        }
    }

    // If error occurs while getting the weather, hides weather layout and shows an error
    private fun errorLayout(){
        linearLayoutMain.visibility = View.GONE
        progressbar.visibility = View.GONE
        weatherBackground.visibility = View.GONE
        errorMsg.visibility = View.VISIBLE
        errorIcon.visibility = View.VISIBLE
        if(!nightMode) {
            weatherRefresh.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        } else {
            weatherRefresh.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
    }
}