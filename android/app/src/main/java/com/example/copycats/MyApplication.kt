package com.example.copycats

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import org.osmdroid.util.GeoPoint
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import com.example.copycats.obj.Event
import com.example.copycats.obj.Location

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var uuid: String

    companion object {
        lateinit var dotenv: MutableMap<String, String>
        var lastKnownUserLocation: GeoPoint? = null
        val locations = mutableListOf<Location>()
        val events = mutableListOf<Event>()
        lateinit var mqttManager: MqttManager
    }

    override fun onCreate(){
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        dotenv = mutableMapOf()
        dotenv.put("API_BASE_URL", "http://10.0.2.2:8000")
        dotenv.put("MQTT_BROKER_URL", "tcp://10.0.2.2:1883")

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        if (!sharedPreferences.contains("uuid")) {
            uuid = UUID.randomUUID().toString()
            sharedPreferences.edit { putString("uuid", uuid) }
            Log.d("[STORAGE] UUID", "Generated UUID: $uuid")
        } else {
            uuid = sharedPreferences.getString("uuid", "")!!
            Log.d("[STORAGE] UUID", "Loaded UUID: $uuid")
        }

        initializeLocationTracking()

        // Initialize and connect MQTT
        mqttManager = MqttManager(this)
        mqttManager.connect(
            onSuccess = {
                Log.d("MQTT", "Connected to MQTT broker on startup")
            },
            onFailure = { error ->
                Log.e("MQTT", "Failed to connect to MQTT broker: $error")
            }
        )

        // Fetch locations from API
        CoroutineScope(Dispatchers.Main).launch {
            fetchLocations()
            Log.d("Locations", "Total locations fetched: ${locations.size}")
            locations.forEach { location ->
                Log.d("Locations", "Location #${location.id}: ${location.info} at (${location.latitude}, ${location.longitude})")
            }
        }

        // Fetch events from API
        CoroutineScope(Dispatchers.Main).launch {
            fetchEvents()
            Log.d("Events", "Total events fetched: ${events.size}")
            events.forEach { event ->
                Log.d("Events", "Event #${event.id}: ${event.title} (${event.start_date} - ${event.end_date}) [tag: ${event.tag ?: "none"}, location_fk: ${event.location_fk ?: "none"}]")
            }
        }
    }

    // potrebni razredi za Application
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    private fun initializeLocationTracking() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            try {
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (lastKnownLocation != null) {
                    lastKnownUserLocation = GeoPoint(lastKnownLocation.latitude, lastKnownLocation.longitude)
                    Log.d("Location", "Cached location: ${lastKnownUserLocation?.latitude}, ${lastKnownUserLocation?.longitude}")
                }

                // Request location updates
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L, // 5 seconds
                    10f,   // 10 meters
                    object : LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            lastKnownUserLocation = GeoPoint(location.latitude, location.longitude)
                            Log.d("Location", "Updated location: ${location.latitude}, ${location.longitude}")
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                )
            } catch (e: SecurityException) {
                Log.e("Location", "Permission denied", e)
            }
        }
    }

    private suspend fun fetchLocations(url: String? = dotenv["API_BASE_URL"]) {
        withContext(Dispatchers.IO) {
            try {
                val apiUrl = "$url/api/location/all"
                Log.d("Locations", "Fetching from: $apiUrl")

                val connection = java.net.URL(apiUrl).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("Locations", "API Response: $response")

                    val jsonArray = JSONArray(response)
                    locations.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        locations.add(Location(
                            id = obj.getInt("id"),
                            info = obj.getString("info"),
                            longitude = obj.getDouble("longitude"),
                            latitude = obj.getDouble("latitude"),
                            locationOutlineFk = if (obj.isNull("location_outline_fk")) null else obj.getInt("location_outline_fk")
                        ))
                    }
                } else {
                    Log.e("Locations", "Failed to fetch locations. HTTP code: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("Locations", "Error fetching locations: ${e.message}", e)
            }
        }
    }

    private suspend fun fetchEvents(url: String? = dotenv["API_BASE_URL"]) {
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
                    events.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        events.add(Event(
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