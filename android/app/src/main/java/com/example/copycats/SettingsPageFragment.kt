package com.example.copycats

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import android.util.Log

class SettingsPageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var sliderUpdateFrequency: Slider
    private lateinit var textUpdateFrequencyStatus: TextView
    private lateinit var editAnnouncementMessage: TextInputEditText
    private lateinit var buttonSendAnnouncement: MaterialButton

    companion object {
        private const val PREF_NAME = "Settings"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_UPDATE_FREQUENCY = "update_frequency_minutes"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        switchNotifications = view.findViewById(R.id.switch_notifications)
        switchDarkMode = view.findViewById(R.id.switch_dark_mode)
        sliderUpdateFrequency = view.findViewById(R.id.slider_update_frequency)
        textUpdateFrequencyStatus = view.findViewById(R.id.text_update_frequency_status)
        editAnnouncementMessage = view.findViewById(R.id.edit_announcement_message)
        buttonSendAnnouncement = view.findViewById(R.id.button_send_announcement)

        loadSettings()

        setupListeners(view)
    }

    private fun loadSettings() {
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
        switchNotifications.isChecked = notificationsEnabled

        val darkModeEnabled = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        switchDarkMode.isChecked = darkModeEnabled

        val updateFrequencyMinutes = sharedPreferences.getInt(KEY_UPDATE_FREQUENCY, 5)
        sliderUpdateFrequency.value = updateFrequencyMinutes.toFloat()
        updateFrequencyText(updateFrequencyMinutes)
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageButton>(R.id.confirm_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MainPageFragment())
                .addToBackStack(null)
                .commit()
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationsSetting(isChecked)
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveDarkModeSetting(isChecked)
            applyDarkMode(isChecked)
        }

        sliderUpdateFrequency.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            saveUpdateFrequencySetting(minutes)
            updateFrequencyText(minutes)
        }

        buttonSendAnnouncement.setOnClickListener {
            val message = editAnnouncementMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendAnnouncementMessage(message)
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNotificationsSetting(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    private fun saveDarkModeSetting(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    private fun saveUpdateFrequencySetting(minutes: Int) {
        sharedPreferences.edit().putInt(KEY_UPDATE_FREQUENCY, minutes).apply()
        SettingsManager.updateFrequencyMinutes = minutes
    }

    private fun applyDarkMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun updateFrequencyText(minutes: Int) {
        textUpdateFrequencyStatus.text = getString(R.string.settings_update_frequency_value, minutes)
    }

    private fun sendAnnouncementMessage(message: String) {
        if (!MyApplication.mqttManager.isConnectedToMqtt()) {
            Toast.makeText(requireContext(), "MQTT not connected. Please wait...", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent spam
        buttonSendAnnouncement.isEnabled = false

        val topic = "announcement/global"
        var successCount = 0
        var failureCount = 0
        val totalEvents = MyApplication.events.size

        // Send to global announcement topic
        MyApplication.mqttManager.publishMessage(
            topic,
            message,
            onSuccess = {
                Log.d("SettingsPage", "Announcement sent to global topic")
            },
            onFailure = { error ->
                Log.e("SettingsPage", "Failed to send to global topic: $error")
            }
        )

        // Send to each event's announcement topic
        if (MyApplication.events.isEmpty()) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Announcement sent!", Toast.LENGTH_SHORT).show()
                editAnnouncementMessage.text?.clear()
                buttonSendAnnouncement.isEnabled = true
            }
            return
        }

        MyApplication.events.forEach { event ->
            val eventTopic = "event/${event.id}/announcement"
            MyApplication.mqttManager.publishMessage(
                eventTopic,
                message,
                onSuccess = {
                    successCount++
                    Log.d("SettingsPage", "Announcement sent to event ${event.id}")
                    if (successCount + failureCount >= totalEvents) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Announcement sent to $successCount/$totalEvents events!",
                                Toast.LENGTH_SHORT
                            ).show()
                            editAnnouncementMessage.text?.clear()
                            buttonSendAnnouncement.isEnabled = true
                        }
                    }
                },
                onFailure = { error ->
                    failureCount++
                    Log.e("SettingsPage", "Failed to send to event ${event.id}: $error")
                    if (successCount + failureCount >= totalEvents) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Announcement sent to $successCount/$totalEvents events",
                                Toast.LENGTH_SHORT
                            ).show()
                            editAnnouncementMessage.text?.clear()
                            buttonSendAnnouncement.isEnabled = true
                        }
                    }
                }
            )
        }
    }
}

