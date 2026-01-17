package com.example.copycats

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttManager(private val context: Context) {
    private var mqttClient: MqttClient? = null
    private val TAG = "MqttManager"
    private var isConnected = false

    init {
        try {
            val brokerUrl = MyApplication.dotenv["MQTT_BROKER_URL"] ?: "tcp://10.0.2.2:1883"
            val clientId = "Android_${System.currentTimeMillis()}"

            Log.d(TAG, "MQTT Client initialized with broker: $brokerUrl, clientId: $clientId")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MQTT client", e)
        }
    }

    fun connect(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val brokerUrl = MyApplication.dotenv["MQTT_BROKER_URL"] ?: "tcp://10.0.2.2:1883"
                val clientId = "Android_${System.currentTimeMillis()}"

                mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 10
                    keepAliveInterval = 60
                }

                mqttClient?.connect(options)
                isConnected = true
                Log.d(TAG, "Connected to MQTT broker")

                // Start listening for sensor data and announcements
                mqttClient?.let { client ->
                    MqttDataListener.startListening(client, context)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            } catch (e: MqttException) {
                isConnected = false
                Log.e(TAG, "Failed to connect to MQTT broker", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure(e.message ?: "Unknown MQTT error")
                }
            } catch (e: Exception) {
                isConnected = false
                Log.e(TAG, "Unexpected error connecting to MQTT broker", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun disconnect(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isConnected && mqttClient?.isConnected == true) {
                    mqttClient?.disconnect()
                    isConnected = false
                    Log.d(TAG, "Disconnected from MQTT broker")

                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess()
                    }
                }
            } catch (e: MqttException) {
                Log.e(TAG, "Error disconnecting from MQTT broker", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun publishMessage(
        topic: String,
        message: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!isConnected || mqttClient?.isConnected != true) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onFailure("MQTT client not connected")
                    }
                    return@launch
                }

                val mqttMessage = MqttMessage(message.toByteArray()).apply {
                    qos = 1
                    isRetained = false
                }

                mqttClient?.publish(topic, mqttMessage)
                Log.d(TAG, "Published message to topic: $topic, message: $message")

                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            } catch (e: MqttException) {
                Log.e(TAG, "Error publishing message to topic: $topic", e)
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun isConnectedToMqtt(): Boolean = isConnected && mqttClient?.isConnected == true
}
