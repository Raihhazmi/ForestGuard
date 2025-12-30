package com.forestguard.app.ui.screen

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    // --- 1. DARK MODE (Pakai StateFlow karena dari DataStore) ---
    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // --- 2. FITUR LAIN (Pakai State Biasa untuk Simulasi) ---
    // Pastikan ini PUBLIC (tidak ada 'private' di depannya)

    private val _isPushEnabled = mutableStateOf(true)
    val isPushEnabled: State<Boolean> = _isPushEnabled

    private val _isAutoSync = mutableStateOf(true)
    val isAutoSync: State<Boolean> = _isAutoSync

    private val _isUploadCellular = mutableStateOf(false)
    val isUploadCellular: State<Boolean> = _isUploadCellular

    private val _isBackgroundLocation = mutableStateOf(true)
    val isBackgroundLocation: State<Boolean> = _isBackgroundLocation

    private val _isMLOnDevice = mutableStateOf(true)
    val isMLOnDevice: State<Boolean> = _isMLOnDevice

    private val _isDebugMode = mutableStateOf(false)
    val isDebugMode: State<Boolean> = _isDebugMode


    // --- FUNGSI ---

    fun toggleDarkMode(value: Boolean) {
        viewModelScope.launch {
            prefs.saveDarkMode(value)
        }
    }

    fun toggleAutoSync(value: Boolean, context: Context) {
        _isAutoSync.value = value
        Toast.makeText(context, "Auto Sync: ${if(value) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
    }

    // Fungsi Setter Sederhana
    fun setPushEnabled(v: Boolean) { _isPushEnabled.value = v }
    fun setUploadCellular(v: Boolean) { _isUploadCellular.value = v }
    fun setBackgroundLocation(v: Boolean) { _isBackgroundLocation.value = v }
    fun setMLOnDevice(v: Boolean) { _isMLOnDevice.value = v }
    fun setDebugMode(v: Boolean) { _isDebugMode.value = v }

    fun clearCache(context: Context) {
        Toast.makeText(context, "Cache dibersihkan", Toast.LENGTH_SHORT).show()
    }

    fun downloadOfflineMaps(context: Context) {
        Toast.makeText(context, "Mengunduh peta...", Toast.LENGTH_SHORT).show()
    }
}