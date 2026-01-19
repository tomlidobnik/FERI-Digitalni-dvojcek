package com.example.copycats.obj

import android.util.Log
import androidx.compose.remote.creation.random
import com.example.copycats.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

object Events {

    val instance: MutableList<Event> = mutableListOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchEvents()
        }
    }

    suspend fun fetchEvents(url: String? = MyApplication.dotenv["API_BASE_URL"]) {
        withContext(Dispatchers.IO) {
            try {
                val apiUrl = "$url/api/event/all"
                Log.d("Events", "Fetching from: $apiUrl")

                val connection = java.net.URL(apiUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("Events", "API Response: $response")

                    val jsonArray = JSONArray(response)
                    instance.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        instance.add(Event(
                            id = obj.getInt("id"),
                            user_fk = obj.getInt("user_fk"),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            start_date = obj.getString("start_date"),
                            end_date = obj.getString("end_date"),
                            location_fk = if (obj.isNull("location_fk")) null else obj.getInt("location_fk"),
                            public = obj.getBoolean("public"),
                            tag = if (obj.isNull("tag")) null else obj.getString("tag"),
                            num_people = (0..10).random()
                        ))
                    }

                    Log.d("Events", "Total events fetched: ${instance.size}")
                    instance.forEach { event ->
                        Log.d("Events", "Event #${event.id}: ${event.title} (${event.start_date} - ${event.end_date})")
                    }
                } else {
                    Log.e("Events", "Failed to fetch events. HTTP code: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("Events", "Error fetching events: ${e.message}", e)
            }
        }
    }
}