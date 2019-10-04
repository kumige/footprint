package com.example.sensorapp

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_weather.*
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class WeatherFragment : Fragment() {

    val apiKey: String = BuildConfig.ApiKey
    val city: String = "helsinki,fi"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    lateinit var progressbar: ProgressBar
    lateinit var linearLayoutMain: LinearLayout
    lateinit var errorMsg: TextView
    lateinit var errorIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        progressbar = view.findViewById(R.id.loader)
        linearLayoutMain = view.findViewById(R.id.mainLinearLayout)
        errorMsg = view.findViewById(R.id.textView_weatherErrorMsg)
        errorIcon = view.findViewById(R.id.errorImg)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         weatherTask().execute()
    }

    inner class weatherTask : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            if(::progressbar.isInitialized && ::linearLayoutMain.isInitialized && ::errorMsg.isInitialized && ::errorIcon.isInitialized) {
                progressbar.visibility = View.VISIBLE
                linearLayoutMain.visibility = View.GONE
                errorMsg.visibility = View.GONE
                errorIcon.visibility = View.GONE
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)

            try{
                Log.d("dbg", "$latitude, $longitude")
                /*fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    latitude = task.result?.latitude
                    longitude = task.result?.longitude
                    Log.d("dbg", "fusedLocationClient $latitude, $longitude")
                    response = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&APPID=$apiKey").readText(
                        Charsets.UTF_8
                    )
                }
                */

                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
                Log.d("dbg", "$e")
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText =
                    "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                        Date(updatedAt * 1000)
                    )
                val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val weatherSimple = weather.getString("main")
                //val temp = main.getString("temp") + "°C"

                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                val tempINT = main.getString("temp")
                Log.d("weather", "tempint: ${tempINT}")
                val tempRounded = BigDecimal(tempINT).setScale(0, RoundingMode.HALF_EVEN).toString()
                Log.d("weather", "temprounded: ${tempRounded}")

                val tempString = "$tempRounded°C"
                Log.d("weather", "${weatherSimple}, ${weatherDescription}")

                weatherColors(weatherSimple, weatherDescription)
                textView_temperature.text = tempString
                textView_weather.text = weatherDescription
                textView_weatherLocation.text = city

                progressbar.visibility = View.GONE
                linearLayoutMain.visibility = View.VISIBLE

            } catch (e: Exception) {
                progressbar.visibility = View.GONE
                errorMsg.visibility = View.VISIBLE
                errorIcon.visibility = View.VISIBLE
            }
        }
    }

    private fun weatherColors(simpleWeather: String, weatherDesc: String){
        when(simpleWeather){
            "Thunderstorm" -> {
                weather_icon.setImageResource(R.drawable.thunder)
                weather_background.setBackgroundResource(R.drawable.weather_background_thunder)
                Log.d("weather", "thunder")
            }

            "Drizzle" -> {
                weather_icon.setImageResource(R.drawable.rain2)
                weather_background.setBackgroundResource(R.drawable.weather_background_rain)
                Log.d("weather","tihkusade")
            }

            "Rain" -> {
                weather_icon.setImageResource(R.drawable.rain2)
                weather_background.setBackgroundResource(R.drawable.weather_background_rain)
                Log.d("weather", "Rain")
            }

            "Snow" -> {
                weather_icon.setImageResource(R.drawable.snow)
                weather_background.setBackgroundResource(R.drawable.weather_background_snow)
                Log.d("weather", "snow")
            }

            "Clear" -> {
                weather_icon.setImageResource(R.drawable.sunny)
                weather_background.setBackgroundResource(R.drawable.weather_background_sunny)
                Log.d("weather", "aurinkoista")
            }

            "Clouds" -> {
                // LISÄÄ PUOLIPILVINEN
                if(weatherDesc == "few clouds: 11-25%") {
                weather_icon.setImageResource(R.drawable.cloudy)
                weather_background.setBackgroundResource(R.drawable.weather_background_cloudy)
                Log.d("weather", "cloudy")
                } else {
                weather_icon.setImageResource(R.drawable.cloudy)
                weather_background.setBackgroundResource(R.drawable.weather_background_cloudy)
                Log.d("weather", "cloudy")
                }
            }

            "Mist" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Smoke" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Haze" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Dust" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Fog" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Sand" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Dust" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Ash" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Squall" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            "Tornado" -> {
                weather_icon.setImageResource(R.drawable.misty)
                weather_background.setBackgroundResource(R.drawable.weather_background_misty)
                Log.d("weather","MIST")
            }

            else -> {
                progressbar.visibility = View.GONE
                linearLayoutMain.visibility = View.GONE
                errorMsg.visibility = View.VISIBLE
                errorIcon.visibility = View.VISIBLE
                errorMsg.text = "Error: Invalid weather"
                Log.d("weather", "Invalid weather")
            }
        }
    }
}