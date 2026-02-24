package com.example.krishisetuapp

import android.util.Log
import org.json.JSONObject
import java.net.URL

class WeatherApiHelper {

    fun willRainInNext48Hours(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): String {

        return try {

            if (apiKey.isBlank()) {
                Log.e("WeatherAPI", "API Key is empty")
                return "ERROR"
            }

            val url =
                "https://api.openweathermap.org/data/2.5/forecast" +
                        "?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric"

            Log.d("WeatherAPI", "URL: $url")

            val response = URL(url).readText()
            val jsonObject = JSONObject(response)

            val forecastList = jsonObject.getJSONArray("list")

            // Check next 16 entries (48 hours)
            for (i in 0 until minOf(16, forecastList.length())) {

                val item = forecastList.getJSONObject(i)
                val weatherArray = item.getJSONArray("weather")
                val mainWeather =
                    weatherArray.getJSONObject(0).getString("main")

                Log.d("WeatherAPI", "Forecast[$i]: $mainWeather")

                if (mainWeather.equals("Rain", true)
                    || mainWeather.equals("Drizzle", true)
                    || mainWeather.equals("Thunderstorm", true)
                ) {
                    return "RAIN"
                }
            }

            return "CLEAR"

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("WeatherAPI", "API ERROR: ${e.message}")
            return "ERROR"
        }
    }
}