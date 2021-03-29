package com.example.trainyourglove.data

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.example.trainyourglove.BuildConfig
import com.example.trainyourglove.connectivity.AppBluetooth

class PreferenceManager private constructor(context: Context) {

    private val prefs =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    fun getLastConnectionAddress(): String {
        return getString(KEY_LAST_CONNECTED_DEVICE_ADDRESS)
    }

    fun setLastConnectionAddress(device: BluetoothDevice) {
        putString(KEY_LAST_CONNECTED_DEVICE_ADDRESS, device.address)
    }

    fun getMaintanedPrimaryKey(): Int {
        val lastPrimaryKey = getInt(KEY_LAST_PRIMARY_KEY)
        val newPrimaryKey = lastPrimaryKey + 1

        // Update last
        putInt(KEY_LAST_PRIMARY_KEY, newPrimaryKey)

        return newPrimaryKey
    }

    private fun putString(key: String, value: String) {
        with(prefs.edit()) {
            putString(key, value)
            apply()
        }
    }

    private fun getString(key: String): String {
        return prefs.getString(key, "") ?: ""
    }

    private fun putInt(key: String, value: Int) {
        with(prefs.edit()) {
            putInt(key, value)
            apply()
        }
    }

    private fun getInt(key: String): Int {
        return prefs.getInt(key, 0)
    }

    companion object {
        private const val KEY_LAST_CONNECTED_DEVICE_ADDRESS = "last_connection_address"
        private const val KEY_LAST_PRIMARY_KEY = "last_primary_key"

        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            var localRef = INSTANCE

            return localRef ?: synchronized(AppBluetooth::class.java) {
                localRef = INSTANCE
                localRef ?: PreferenceManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}