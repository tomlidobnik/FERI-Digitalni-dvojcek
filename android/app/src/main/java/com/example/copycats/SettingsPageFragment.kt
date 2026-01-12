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
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsPageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var sliderUpdateFrequency: Slider
    private lateinit var textUpdateFrequencyStatus: TextView

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
}

