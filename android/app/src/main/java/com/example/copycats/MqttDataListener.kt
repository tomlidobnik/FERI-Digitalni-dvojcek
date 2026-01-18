package com.example.copycats

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class EventSensorData(
    var temperature: Float? = null,
    var soundLevel: Double? = null,
    var lastUpdated: Long = 0
)

object MqttDataListener : MqttCallback {
    private val TAG = "MqttDataListener"
    private val eventSensorData = ConcurrentHashMap<Int, EventSensorData>()
    private val listeners = mutableListOf<(Int, EventSensorData) -> Unit>()
    private var appContext: Context? = null

    fun startListening(mqttClient: MqttClient, context: Context) {
        try {
            appContext = context
            mqttClient.setCallback(this)

            // Subscribe to all event temperature topics
            mqttClient.subscribe("event/+/temperature", 1)
            Log.d(TAG, "Subscribed to event/+/temperature")

            // Subscribe to all event sound_level topics
            mqttClient.subscribe("event/+/sound_level", 1)
            Log.d(TAG, "Subscribed to event/+/sound_level")

            // Subscribe to announcement topics
            mqttClient.subscribe("announcement/global", 1)
            Log.d(TAG, "Subscribed to announcement/global")

            mqttClient.subscribe("event/+/announcement", 1)
            Log.d(TAG, "Subscribed to event/+/announcement")

        } catch (e: MqttException) {
            Log.e(TAG, "Failed to subscribe to topics", e)
        }
    }

    fun addDataListener(listener: (Int, EventSensorData) -> Unit) {
        listeners.add(listener)
    }

    fun removeDataListener(listener: (Int, EventSensorData) -> Unit) {
        listeners.remove(listener)
    }

    fun getSensorData(eventId: Int): EventSensorData? {
        return eventSensorData[eventId]
    }

    override fun connectionLost(cause: Throwable?) {
        Log.w(TAG, "MQTT connection lost", cause)
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        try {
            val payload = String(message.payload)
            Log.d(TAG, "Message received - Topic: $topic, Payload: $payload")
            val parts = topic.split("/")
            Log.d(TAG, "Parts: $parts")

            // Parse topic to extract event ID and data type
            // Format: event/{eventId}/temperature or event/{eventId}/sound_level or event/{eventId}/announcement

            if (parts[0] == "announcement") {
                Log.d(TAG, "Announcement received: $payload")

                // Try to parse as JSON to extract message field
                val displayMessage = try {
                    val json = JSONObject(payload)
                    json.optString("message", payload) // Use "message" field if exists, otherwise use raw payload
                } catch (e: Exception) {
                    payload // If not JSON, use raw payload
                }

                // Show Toast on main thread
                appContext?.let { context ->
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show()
                    }

                    NotificationHelper.showAnnouncementNotification(
                        context,
                        "📢 Global Announcement",
                        displayMessage
                    )
                }
                return
            }

            if (parts.size >= 3 && parts[0] == "event") {
                val eventId = parts[1].toIntOrNull() ?: return
                val dataType = parts[2]

                when (dataType) {
                    "temperature" -> {
                        val data = eventSensorData.getOrPut(eventId) { EventSensorData() }
                        data.temperature = payload.toFloatOrNull()
                        data.lastUpdated = System.currentTimeMillis()
                        Log.d(TAG, "Updated temperature for event $eventId: ${data.temperature}°C")

                        // Notify all listeners
                        listeners.forEach { it(eventId, data) }
                    }
                    "sound_level" -> {
                        val data = eventSensorData.getOrPut(eventId) { EventSensorData() }
                        data.soundLevel = payload.toDoubleOrNull()
                        data.lastUpdated = System.currentTimeMillis()
                        Log.d(TAG, "Updated sound level for event $eventId: ${data.soundLevel} dB")

                        // Notify all listeners
                        listeners.forEach { it(eventId, data) }
                    }
                    "announcement" -> {
                        // Event-specific announcement
                        Log.d(TAG, "Event announcement received: $payload")

                        // Try to parse as JSON to extract message field
                        val displayMessage = try {
                            val json = JSONObject(payload)
                            json.optString("message", payload)
                        } catch (e: Exception) {
                            payload
                        }

                        appContext?.let { context ->
                            val event = MyApplication.events.find { it.id == eventId }
                            val eventTitle = event?.title ?: "Event #$eventId"

                            // Show Toast on main thread
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show()
                            }

                            NotificationHelper.showAnnouncementNotification(
                                context,
                                "📢 $eventTitle",
                                displayMessage,
                                eventId
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Not needed for subscription
    }
}
