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
) {
    fun copy(): EventSensorData {
        return EventSensorData(
            temperature = this.temperature,
            soundLevel = this.soundLevel,
            lastUpdated = this.lastUpdated
        )
    }
}

object MqttDataListener : MqttCallback {
    private val TAG = "MqttDataListener"
    private val eventSensorData = ConcurrentHashMap<Int, EventSensorData>()
    private val listeners = mutableListOf<(Int, EventSensorData) -> Unit>()
    private var appContext: Context? = null

    fun startListening(mqttClient: MqttClient, context: Context) {
        try {
            appContext = context
            mqttClient.setCallback(this)

            mqttClient.subscribe("event/+/temperature", 1)
            Log.d(TAG, "Subscribed to event/+/temperature")

            mqttClient.subscribe("event/+/sound_level", 1)
            Log.d(TAG, "Subscribed to event/+/sound_level")

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
        Log.d(TAG, "Listener added - Total listeners: ${listeners.size}")
    }

    fun removeDataListener(listener: (Int, EventSensorData) -> Unit) {
        listeners.remove(listener)
        Log.d(TAG, "Listener removed - Total listeners: ${listeners.size}")
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
            Log.d(TAG, "==========================================")
            Log.d(TAG, "MQTT MESSAGE ARRIVED!")
            Log.d(TAG, "Topic: $topic")
            Log.d(TAG, "Payload: $payload")
            Log.d(TAG, "==========================================")

            val parts = topic.split("/")
            Log.d(TAG, "Topic parts: $parts")

            if (parts[0] == "announcement") {
                Log.d(TAG, "Announcement received: $payload")

                val displayMessage = try {
                    val json = JSONObject(payload)
                    json.optString("message", payload)
                } catch (e: Exception) {
                    payload
                }

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
                        Log.d(TAG, "Notifying ${listeners.size} listener(s) about temperature update")

                        listeners.forEach {
                            Log.d(TAG, "Calling listener for event $eventId")
                            it(eventId, data)
                        }
                    }
                    "sound_level" -> {
                        val data = eventSensorData.getOrPut(eventId) { EventSensorData() }
                        data.soundLevel = payload.toDoubleOrNull()
                        data.lastUpdated = System.currentTimeMillis()
                        Log.d(TAG, "Updated sound level for event $eventId: ${data.soundLevel} dB")
                        Log.d(TAG, "Notifying ${listeners.size} listener(s) about sound level update")

                        listeners.forEach {
                            Log.d(TAG, "Calling listener for event $eventId")
                            it(eventId, data)
                        }
                    }
                    "announcement" -> {
                        Log.d(TAG, "Event announcement received: $payload")

                        val displayMessage = try {
                            val json = JSONObject(payload)
                            json.optString("message", payload)
                        } catch (e: Exception) {
                            payload
                        }

                        appContext?.let { context ->
                            val event = MyApplication.events.find { it.id == eventId }
                            val eventTitle = event?.title ?: "Event #$eventId"

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
    }
}
