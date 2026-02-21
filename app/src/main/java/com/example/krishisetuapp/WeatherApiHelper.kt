package com.example.krishisetuapp
import com.example.krishisetuapp.BuildConfig

import android.util.Log
import org.json.JSONObject
import java.net.URL

class WeatherApiHelper {

    companion object {
        private const val TAG = "WeatherApiHelper"
    }

    fun willRainInNext48Hours(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): Boolean {

        // 🔐 Safety check
        if (apiKey.isBlank() || apiKey == "null") {
            Log.e(TAG, "API key is missing")
            return false
        }

        return try {

            val url =
                "https://api.openweathermap.org/data/2.5/forecast" +
                        "?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"

            Log.d(TAG, "Calling URL: $url")

            val response = URL(url).readText()
            val jsonObject = JSONObject(response)

            val cod = jsonObject.getString("cod")
            if (cod != "200") {
                Log.e(TAG, "API Error Code: $cod")
                return false
            }

            val forecastList = jsonObject.getJSONArray("list")

            // 48 hours = 16 entries (3-hour interval)
            for (i in 0 until minOf(16, forecastList.length())) {

                val item = forecastList.getJSONObject(i)
                val weatherArray = item.getJSONArray("weather")
                val mainWeather =
                    weatherArray.getJSONObject(0).getString("main")

                Log.d(TAG, "Forecast[$i]: $mainWeather")

                if (
                    mainWeather.equals("Rain", true) ||
                    mainWeather.equals("Drizzle", true) ||
                    mainWeather.equals("Thunderstorm", true)
                ) {
                    return true
                }
            }

            false

        } catch (e: Exception) {
            Log.e(TAG, "Weather API failed", e)
            false
        }
    }
}