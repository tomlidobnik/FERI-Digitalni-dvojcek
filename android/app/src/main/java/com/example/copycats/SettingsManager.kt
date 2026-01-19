package com.example.copycats

import android.content.Context
import android.content.SharedPreferences
object SettingsManager {

    private const val PREF_NAME = "Settings"
    private const val KEY_NOTIFICATIONS = "notifications_enabled"
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    private const val KEY_UPDATE_FREQUENCY = "update_frequency_minutes"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Notifications
    var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    // Dark Mode
    var darkModeEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DARK_MODE, value).apply()

    // Update Frequency (in minutes, default 5 minutes)
    var updateFrequencyMinutes: Int
        get() = sharedPreferences.getInt(KEY_UPDATE_FREQUENCY, 5)
        set(value) = sharedPreferences.edit().putInt(KEY_UPDATE_FREQUENCY, value).apply()

    // zanka za posodabljanje
    fun getUpdateIntervalMillis(): Long {
        return updateFrequencyMinutes * 60 * 1000L
    }
}

