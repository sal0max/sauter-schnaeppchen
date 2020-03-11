package de.salomax.sauterschnaeppchen.data

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    val sharedPrefs: SharedPreferences,
    private val key: String,
    private val defValue: T
) : LiveData<T>() {

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == this.key) {
                value = getValueFromPreferences(key, defValue)
            }
        }

    abstract fun getValueFromPreferences(key: String, defValue: T): T

    override fun onActive() {
        super.onActive()
        value = getValueFromPreferences(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}

class SharedPreferenceStringLiveData(
    sharedPrefs: SharedPreferences,
    key: String,
    defValue: String?
) : SharedPreferenceLiveData<String?>(sharedPrefs, key, defValue) {
    override fun getValueFromPreferences(key: String, defValue: String?): String? =
        sharedPrefs.getString(key, defValue)
}
