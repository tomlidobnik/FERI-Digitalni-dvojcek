package com.example.copycats

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.copycats.SimulationFragment.Companion.KEY_IS_RUNNING
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class SimulationFragment : Fragment() {

    private lateinit var simulationToggle: SwitchMaterial
    private lateinit var simulationStatus: TextView

    private lateinit var sliderSoundMin: Slider
    private lateinit var sliderSoundMax: Slider
    private lateinit var textSoundMin: TextView
    private lateinit var textSoundMax: TextView

    private lateinit var sliderTempMin: Slider
    private lateinit var sliderTempMax: Slider
    private lateinit var textTempMin: TextView
    private lateinit var textTempMax: TextView

    private lateinit var sliderFreqMin: Slider
    private lateinit var sliderFreqMax: Slider
    private lateinit var textFreqMin: TextView
    private lateinit var textFreqMax: TextView

    companion object {
        @JvmStatic
        fun newInstance() = SimulationFragment()
        private var backgroundJob: Job? = null
        private var isSimulationRunning = false
        private var soundMin = 30f
        private var soundMax = 80f
        private var tempMin = 15f
        private var tempMax = 30f
        private var freqMin = 2
        private var freqMax = 10

        private const val PREFS_NAME = "SimulationPrefs"
        private const val KEY_SOUND_MIN = "sound_min"
        private const val KEY_SOUND_MAX = "sound_max"
        private const val KEY_TEMP_MIN = "temp_min"
        private const val KEY_TEMP_MAX = "temp_max"
        private const val KEY_FREQ_MIN = "freq_min"
        private const val KEY_FREQ_MAX = "freq_max"
        private const val KEY_IS_RUNNING = "is_running"

        fun startBackgroundSimulation(context: Context) {
            if (isSimulationRunning) return

            isSimulationRunning = true
            saveSimulationState(context, true)

            backgroundJob = CoroutineScope(Dispatchers.IO).launch {
                Log.d("Simulation", "Background simulation started")

                // Start individual simulation for each event
                MyApplication.events.forEach { event ->
                    if (isActive && isSimulationRunning) {
                        launch {
                            simulateEventData(event.id)
                        }
                    }
                }
            }
        }

        private suspend fun simulateEventData(eventId: Int) {
            val initialDelay = Random.nextInt(0, freqMax + 1) * 1000L
            delay(initialDelay)

            while (isSimulationRunning) {
                try {
                    sendSimulatedDataForEvent(eventId)

                    val randomDelay = Random.nextInt(freqMin, freqMax + 1) * 1000L
                    Log.d("Simulation", "Event $eventId next update in ${randomDelay / 1000} seconds")
                    delay(randomDelay)
                } catch (e: CancellationException) {
                    Log.d("Simulation", "Simulation cancelled for event $eventId")
                    break
                } catch (e: Exception) {
                    Log.e("Simulation", "Error in simulation for event $eventId", e)
                }
            }
            Log.d("Simulation", "Simulation stopped for event $eventId")
        }

        fun stopBackgroundSimulation(context: Context) {
            isSimulationRunning = false
            backgroundJob?.cancel()
            backgroundJob = null
            saveSimulationState(context, false)
            Log.d("Simulation", "Simulation stopped")
        }

        fun isRunning(): Boolean = isSimulationRunning

        fun updateRanges(sMin: Float, sMax: Float, tMin: Float, tMax: Float, fMin: Int, fMax: Int, context: Context) {
            soundMin = sMin
            soundMax = sMax
            tempMin = tMin
            tempMax = tMax
            freqMin = fMin
            freqMax = fMax
            saveRanges(context)
        }

        private fun sendSimulatedDataForEvent(eventId: Int) {
            if (!MyApplication.mqttManager.isConnectedToMqtt()) {
                Log.w("Simulation", "MQTT not connected, skipping simulation data for event $eventId")
                return
            }

            val event = MyApplication.events.find { it.id == eventId }
            if (event == null) {
                Log.w("Simulation", "Event $eventId not found")
                return
            }

            val soundLevel = Random.nextDouble(soundMin.toDouble(), soundMax.toDouble())
            val temperature = Random.nextDouble(tempMin.toDouble(), tempMax.toDouble())

            MyApplication.mqttManager.publishMessage(
                "event/${event.id}/sound_level",
                String.format(Locale.US, "%.1f", soundLevel),
                onSuccess = {
                    Log.d("Simulation", "Event ${event.id}: Sent sound level ${String.format(Locale.US, "%.1f", soundLevel)} dB")
                },
                onFailure = { error ->
                    Log.e("Simulation", "Failed to send sound level to event ${event.id}: $error")
                }
            )

            MyApplication.mqttManager.publishMessage(
                "event/${event.id}/temperature",
                String.format(Locale.US, "%.1f", temperature),
                onSuccess = {
                    Log.d("Simulation", "Event ${event.id}: Sent temperature ${String.format(Locale.US, "%.1f", temperature)} °C")
                },
                onFailure = { error ->
                    Log.e("Simulation", "Failed to send temperature to event ${event.id}: $error")
                }
            )
        }

        private fun saveSimulationState(context: Context, running: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putBoolean(KEY_IS_RUNNING, running)
                apply()
            }
        }

        private fun saveRanges(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putFloat(KEY_SOUND_MIN, soundMin)
                putFloat(KEY_SOUND_MAX, soundMax)
                putFloat(KEY_TEMP_MIN, tempMin)
                putFloat(KEY_TEMP_MAX, tempMax)
                putInt(KEY_FREQ_MIN, freqMin)
                putInt(KEY_FREQ_MAX, freqMax)
                apply()
            }
        }

        fun loadSimulationState(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            soundMin = prefs.getFloat(KEY_SOUND_MIN, 30f)
            soundMax = prefs.getFloat(KEY_SOUND_MAX, 80f)
            tempMin = prefs.getFloat(KEY_TEMP_MIN, 15f)
            tempMax = prefs.getFloat(KEY_TEMP_MAX, 30f)
            freqMin = prefs.getInt(KEY_FREQ_MIN, 2)
            freqMax = prefs.getInt(KEY_FREQ_MAX, 10)

            val wasRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
            if (wasRunning && !isSimulationRunning) {
                startBackgroundSimulation(context)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simulation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadSavedValues()
        setupListeners(view)
        updateUIFromBackgroundState()
    }

    private fun loadSavedValues() {
        sliderSoundMin.value = soundMin
        sliderSoundMax.value = soundMax
        sliderTempMin.value = tempMin
        sliderTempMax.value = tempMax
        sliderFreqMin.value = freqMin.toFloat()
        sliderFreqMax.value = freqMax.toFloat()

        textSoundMin.text = "${soundMin.toInt()} dB"
        textSoundMax.text = "${soundMax.toInt()} dB"
        textTempMin.text = "${tempMin.toInt()} °C"
        textTempMax.text = "${tempMax.toInt()} °C"
        textFreqMin.text = "$freqMin seconds"
        textFreqMax.text = "$freqMax seconds"
    }

    private fun updateUIFromBackgroundState() {
        simulationToggle.isChecked = isRunning()
        simulationStatus.text = if (isRunning()) {
            "Simulation running in background..."
        } else {
            "Simulation stopped"
        }
    }

    private fun initializeViews(view: View) {
        simulationToggle = view.findViewById(R.id.simulation_toggle)
        simulationStatus = view.findViewById(R.id.simulation_status)

        sliderSoundMin = view.findViewById(R.id.slider_sound_min)
        sliderSoundMax = view.findViewById(R.id.slider_sound_max)
        textSoundMin = view.findViewById(R.id.text_sound_min)
        textSoundMax = view.findViewById(R.id.text_sound_max)

        sliderTempMin = view.findViewById(R.id.slider_temp_min)
        sliderTempMax = view.findViewById(R.id.slider_temp_max)
        textTempMin = view.findViewById(R.id.text_temp_min)
        textTempMax = view.findViewById(R.id.text_temp_max)

        sliderFreqMin = view.findViewById(R.id.slider_freq_min)
        sliderFreqMax = view.findViewById(R.id.slider_freq_max)
        textFreqMin = view.findViewById(R.id.text_freq_min)
        textFreqMax = view.findViewById(R.id.text_freq_max)
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        simulationToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateRanges(
                    sliderSoundMin.value,
                    sliderSoundMax.value,
                    sliderTempMin.value,
                    sliderTempMax.value,
                    sliderFreqMin.value.toInt(),
                    sliderFreqMax.value.toInt(),
                    requireContext()
                )
                startBackgroundSimulation(requireContext())
                simulationStatus.text = "Simulation running in background..."
            } else {
                stopBackgroundSimulation(requireContext())
                simulationStatus.text = "Simulation stopped"
            }
        }

        sliderSoundMin.addOnChangeListener { _, value, _ ->
            textSoundMin.text = "${value.toInt()} dB"
            if (value > sliderSoundMax.value) {
                sliderSoundMax.value = value
            }
            updateRangesIfRunning()
        }

        sliderSoundMax.addOnChangeListener { _, value, _ ->
            textSoundMax.text = "${value.toInt()} dB"
            if (value < sliderSoundMin.value) {
                sliderSoundMin.value = value
            }
            updateRangesIfRunning()
        }

        sliderTempMin.addOnChangeListener { _, value, _ ->
            textTempMin.text = "${value.toInt()} °C"
            if (value > sliderTempMax.value) {
                sliderTempMax.value = value
            }
            updateRangesIfRunning()
        }

        sliderTempMax.addOnChangeListener { _, value, _ ->
            textTempMax.text = "${value.toInt()} °C"
            if (value < sliderTempMin.value) {
                sliderTempMin.value = value
            }
            updateRangesIfRunning()
        }

        sliderFreqMin.addOnChangeListener { _, value, _ ->
            textFreqMin.text = "${value.toInt()} seconds"
            if (value > sliderFreqMax.value) {
                sliderFreqMax.value = value
            }
            updateRangesIfRunning()
        }

        sliderFreqMax.addOnChangeListener { _, value, _ ->
            textFreqMax.text = "${value.toInt()} seconds"
            if (value < sliderFreqMin.value) {
                sliderFreqMin.value = value
            }
            updateRangesIfRunning()
        }
    }

    private fun updateRangesIfRunning() {
        if (isRunning()) {
            updateRanges(
                sliderSoundMin.value,
                sliderSoundMax.value,
                sliderTempMin.value,
                sliderTempMax.value,
                sliderFreqMin.value.toInt(),
                sliderFreqMax.value.toInt(),
                requireContext()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
