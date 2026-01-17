package com.example.copycats.obj

import com.example.copycats.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.addAll
import kotlin.text.clear
import kotlin.text.get

object Locations {

    val instance: MutableList<Location> = mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchLocations()
        }
    }

    suspend fun fetchLocations(
        url: String? = MyApplication.dotenv["API_BASE_URL"]
    ) {
        withContext(Dispatchers.IO) {
            try {
                val apiUrl = "$url/api/location/all"
                val connection = java.net.URL(apiUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    // Parse JSON response
                    val jsonArray = org.json.JSONArray(response)
                    val locations = mutableListOf<Location>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val location = Location(
                            id = jsonObject.getInt("id"),
                            info = jsonObject.getString("info"),
                            longitude = jsonObject.getDouble("longitude"),
                            latitude = jsonObject.getDouble("latitude"),
                            locationOutlineFk = jsonObject.optInt("location_outline_fk", -1).takeIf { it != -1 }
                        )
                        locations.add(location)
                    }

                    instance.clear()
                    instance.addAll(locations)
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}