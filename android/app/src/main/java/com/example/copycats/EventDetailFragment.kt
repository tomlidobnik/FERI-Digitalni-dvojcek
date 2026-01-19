package com.example.copycats

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.copycats.obj.Event
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10

class EventDetailFragment : Fragment(), SensorEventListener {

    private var eventId: Int = -1
    private var sensorManager: SensorManager? = null
    private var temperatureSensor: Sensor? = null
    private var currentTemperature: Float = 0f
    private var hasValidTemperature = false
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentSoundLevel: Double = 0.0

    private var loudspeakerEnabled = false
    private var temperatureEnabled = false
    private var useMockTemperature = false
    private var isMockTemperatureRunning = false

    private val mqttSendHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var mqttSendRunnable: Runnable? = null

    private val PREFS_NAME = "EventSensorPrefs"
    private val KEY_LOUDSPEAKER_ENABLED = "loudspeaker_enabled_"
    private val KEY_TEMPERATURE_ENABLED = "temperature_enabled_"

    private val micPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSoundLevelMonitoring()
        } else {
            Toast.makeText(requireContext(), "Microphone permission required for sound monitoring", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getInt(ARG_EVENT_ID, -1)
        }

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        if (temperatureSensor != null) {
            Log.i("EventDetail", "Using ambient temperature sensor")
            useMockTemperature = false
        } else {
            Log.w("EventDetail", "No ambient temperature sensor available - using mock temperature for testing")
            useMockTemperature = true
        }

        val random = java.util.Random()
        currentTemperature = 20f + (random.nextDouble() * 6f - 3f).toFloat()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val event = MyApplication.events.find { it.id == eventId }

        if (event != null) {
            displayEventDetails(view, event)
            setupSensorControls(view, event)
        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.take_photo_button).setOnClickListener {
            val cameraFragment = CameraFragment.newInstance(eventId)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, cameraFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun displayEventDetails(view: View, event: Event) {
        view.findViewById<TextView>(R.id.event_title).text = event.title

        val eventIcon = view.findViewById<ImageView>(R.id.event_icon)
        val iconDrawable = when (event.tag?.lowercase()) {
            "education" -> R.drawable.education
            "fun" -> R.drawable.`fun`
            "sports" -> R.drawable.sports
            else -> R.drawable.cat
        }
        eventIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), iconDrawable))

        val numPeople = event.num_people ?: 0
        view.findViewById<TextView>(R.id.event_attendee_count).text =
            getString(R.string.attendee_count, numPeople)

        view.findViewById<TextView>(R.id.event_description).text = event.description

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

        try {
            val startDate = inputFormat.parse(event.start_date)
            val endDate = inputFormat.parse(event.end_date)

            view.findViewById<TextView>(R.id.event_start_date).text =
                if (startDate != null) outputFormat.format(startDate) else event.start_date

            view.findViewById<TextView>(R.id.event_end_date).text =
                if (endDate != null) outputFormat.format(endDate) else event.end_date
        } catch (_: Exception) {
            // If parsing fails, use the original strings
            view.findViewById<TextView>(R.id.event_start_date).text = event.start_date
            view.findViewById<TextView>(R.id.event_end_date).text = event.end_date
        }

        // Set event tag
        view.findViewById<TextView>(R.id.event_tag).text =
            event.tag?.replaceFirstChar { it.uppercase() } ?: "None"

        view.findViewById<TextView>(R.id.event_user_fk).text = event.user_fk.toString()

        view.findViewById<TextView>(R.id.event_public).text =
            if (event.public) "Public" else "Private"

        val location = MyApplication.locations.find { it.id == event.location_fk }
        if (location != null) {
            view.findViewById<TextView>(R.id.event_location_info).text = location.info
            view.findViewById<TextView>(R.id.event_location_coords).text =
                String.format(Locale.US, "(%.4f, %.4f)", location.latitude, location.longitude)

            view.findViewById<MaterialButton>(R.id.show_on_map_button).setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        } else {
            view.findViewById<TextView>(R.id.event_location_info).text = "No location"
            view.findViewById<TextView>(R.id.event_location_coords).visibility = View.GONE
            view.findViewById<MaterialButton>(R.id.show_on_map_button).isEnabled = false
        }
    }

    private fun setupSensorControls(view: View, event: Event) {
        val loudspeakerToggle = view.findViewById<SwitchMaterial>(R.id.loudspeaker_toggle)
        val loudspeakerValue = view.findViewById<TextView>(R.id.loudspeaker_value)
        val temperatureToggle = view.findViewById<SwitchMaterial>(R.id.temperature_toggle)
        val temperatureValue = view.findViewById<TextView>(R.id.temperature_value)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLoudspeakerEnabled = prefs.getBoolean(KEY_LOUDSPEAKER_ENABLED + eventId, false)
        val savedTemperatureEnabled = prefs.getBoolean(KEY_TEMPERATURE_ENABLED + eventId, false)

        loudspeakerToggle.isChecked = savedLoudspeakerEnabled
        temperatureToggle.isChecked = savedTemperatureEnabled

        loudspeakerToggle.setOnCheckedChangeListener { _, isChecked ->
            loudspeakerEnabled = isChecked

            prefs.edit().putBoolean(KEY_LOUDSPEAKER_ENABLED + eventId, isChecked).apply()

            if (isChecked) {
                checkMicrophonePermission()
            } else {
                stopSoundLevelMonitoring()
            }
            updateMqttSendingStatus(event)
        }

        temperatureToggle.setOnCheckedChangeListener { _, isChecked ->
            temperatureEnabled = isChecked

            prefs.edit().putBoolean(KEY_TEMPERATURE_ENABLED + eventId, isChecked).apply()

            if (isChecked) {
                if (useMockTemperature) {
                    startMockTemperatureUpdates()
                    Toast.makeText(requireContext(), "Using simulated temperature (emulator mode)", Toast.LENGTH_SHORT).show()
                } else if (temperatureSensor != null) {
                    sensorManager?.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
                } else {
                    Toast.makeText(requireContext(), "No temperature sensor available", Toast.LENGTH_SHORT).show()
                    temperatureToggle.isChecked = false
                }
            } else {
                if (!useMockTemperature) {
                    sensorManager?.unregisterListener(this, temperatureSensor)
                }
                hasValidTemperature = false
            }
            updateMqttSendingStatus(event)
        }

        if (savedLoudspeakerEnabled) {
            loudspeakerEnabled = true
            checkMicrophonePermission()
            updateMqttSendingStatus(event)
        }

        if (savedTemperatureEnabled) {
            temperatureEnabled = true
            if (useMockTemperature) {
                startMockTemperatureUpdates()
            } else if (temperatureSensor != null) {
                sensorManager?.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            updateMqttSendingStatus(event)
        }

        // Update UI periodically with sensor values
        view.postDelayed(object : Runnable {
            override fun run() {
                if (loudspeakerEnabled && isRecording) {
                    loudspeakerValue.text = String.format(Locale.US, "%.1f dB", currentSoundLevel)
                } else {
                    loudspeakerValue.text = "-- dB"
                }

                if (temperatureEnabled && hasValidTemperature) {
                    temperatureValue.text = String.format(Locale.US, "%.1f °C", currentTemperature)
                } else if (temperatureEnabled) {
                    temperatureValue.text = String.format(Locale.US, "%.1f °C", currentTemperature)
                } else {
                    temperatureValue.text = "-- °C"
                }

                view.postDelayed(this, 500)
            }
        }, 500)
    }

    private fun updateMqttSendingStatus(event: Event) {
        // Stop any existing periodic sending
        mqttSendRunnable?.let { mqttSendHandler.removeCallbacks(it) }

        // Start periodic sending if any sensor is enabled
        if (loudspeakerEnabled || temperatureEnabled) {
            startPeriodicMqttSending(event)
        }
    }

    private fun startPeriodicMqttSending(event: Event) {
        val sendInterval = SettingsManager.getUpdateIntervalMillis()

        mqttSendRunnable = object : Runnable {
            override fun run() {
                sendMqttData(event)
                mqttSendHandler.postDelayed(this, sendInterval)
            }
        }

        // Send immediately first time, then periodically
        sendMqttData(event)
        mqttSendRunnable?.let { mqttSendHandler.postDelayed(it, sendInterval) }

        Log.d("EventDetail", "Started periodic MQTT sending every ${sendInterval / 1000} seconds")
    }

    private fun sendMqttData(event: Event) {
        if (!MyApplication.mqttManager.isConnectedToMqtt()) {
            Log.w("EventDetail", "MQTT not connected, skipping data send")
            return
        }

        val messagesToSend = mutableListOf<Pair<String, String>>()

        // Sound level message
        if (loudspeakerEnabled && isRecording) {
            val topic = "event/${event.id}/sound_level"
            messagesToSend.add(Pair(topic, currentSoundLevel.toString()))
        }

        // Temperature message
        if (temperatureEnabled) {
            val topic = "event/${event.id}/temperature"
            messagesToSend.add(Pair(topic, currentTemperature.toString()))
        }

        if (messagesToSend.isEmpty()) {
            return
        }

        // Send all messages
        for ((topic, message) in messagesToSend) {
            MyApplication.mqttManager.publishMessage(
                topic,
                message,
                onSuccess = {
                    Log.d("EventDetail", "Published to $topic: $message")
                },
                onFailure = { error ->
                    Log.e("EventDetail", "Failed to publish to $topic: $error")
                }
            )
        }
    }

    private fun checkMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startSoundLevelMonitoring()
            }
            else -> {
                micPermissionRequest.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startSoundLevelMonitoring() {
        try {
            // Create a temporary file for MediaRecorder output
            val outputFile = requireContext().cacheDir.absolutePath + "/sound_monitor.3gp"

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            isRecording = true

            // Start monitoring sound level
            view?.postDelayed(object : Runnable {
                override fun run() {
                    if (isRecording && loudspeakerEnabled) {
                        mediaRecorder?.let {
                            val amplitude = it.maxAmplitude
                            currentSoundLevel = if (amplitude > 0) {
                                20 * log10(amplitude.toDouble())
                            } else {
                                0.0
                            }
                        }
                        view?.postDelayed(this, 100)
                    }
                }
            }, 100)

            Log.d("EventDetail", "Sound level monitoring started")
        } catch (e: Exception) {
            Log.e("EventDetail", "Error starting sound monitoring", e)
            Toast.makeText(requireContext(), "Failed to start sound monitoring", Toast.LENGTH_SHORT).show()
            isRecording = false
        }
    }

    private fun stopSoundLevelMonitoring() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            currentSoundLevel = 0.0

            // Clean up temporary file
            try {
                val outputFile = java.io.File(requireContext().cacheDir.absolutePath + "/sound_monitor.3gp")
                if (outputFile.exists()) {
                    outputFile.delete()
                }
            } catch (e: Exception) {
                Log.w("EventDetail", "Failed to delete temporary sound file", e)
            }

            Log.d("EventDetail", "Sound level monitoring stopped")
        } catch (e: Exception) {
            Log.e("EventDetail", "Error stopping sound monitoring", e)
        }
    }

    private fun startMockTemperatureUpdates() {
        if (isMockTemperatureRunning.not()) {
            isMockTemperatureRunning = true
            currentTemperature = 20f + (Math.random() * 6f - 3f).toFloat()
            hasValidTemperature = true
            Log.d("EventDetail", "Mock temperature monitoring started")
        }

        view?.postDelayed(object : Runnable {
            override fun run() {
                if (temperatureEnabled && useMockTemperature && isMockTemperatureRunning) {
                    val change = (Math.random() * 0.4 - 0.2).toFloat()
                    currentTemperature = (currentTemperature + change).coerceIn(18f, 26f)
                    hasValidTemperature = true
                    Log.d("EventDetail", "Mock temperature: $currentTemperature °C")
                    view?.postDelayed(this, 2000)
                }
            }
        }, 2000)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                val temp = it.values[0]
                if (temp > -100f && temp < 100f) {
                    currentTemperature = temp
                    hasValidTemperature = true
                    Log.d("EventDetail", "Valid temperature: $currentTemperature °C")
                } else {
                    hasValidTemperature = false
                    Log.w("EventDetail", "Invalid temperature reading: $temp °C - ignoring")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        isMockTemperatureRunning = false
        stopSoundLevelMonitoring()
    }

    override fun onResume() {
        super.onResume()
        if (temperatureEnabled) {
            if (useMockTemperature) {
                startMockTemperatureUpdates()
            } else if (temperatureSensor != null) {
                sensorManager?.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (loudspeakerEnabled) {
            checkMicrophonePermission()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager?.unregisterListener(this)
        isMockTemperatureRunning = false
        stopSoundLevelMonitoring()

        mqttSendRunnable?.let { mqttSendHandler.removeCallbacks(it) }
        mqttSendRunnable = null
    }

    companion object {
        private const val ARG_EVENT_ID = "event_id"

        @JvmStatic
        fun newInstance(eventId: Int) =
            EventDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EVENT_ID, eventId)
                }
            }
    }
}
