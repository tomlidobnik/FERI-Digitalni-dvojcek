package com.example.copycats

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.util.UUID
import androidx.core.content.edit

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    lateinit var uuid: String

    override fun onCreate(){
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        if (!sharedPreferences.contains("uuid")) {
            uuid = UUID.randomUUID().toString()
            sharedPreferences.edit { putString("uuid", uuid) }
            Log.d("[STORAGE] UUID", "Generated UUID: $uuid")
        } else {
            uuid = sharedPreferences.getString("uuid", "")!!
            Log.d("[STORAGE] UUID", "Loaded UUID: $uuid")
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
}