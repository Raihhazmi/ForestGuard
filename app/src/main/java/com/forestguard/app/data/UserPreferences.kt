package com.forestguard.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        // TAMBAHAN BARU
        val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    }

    // Baca Dark Mode
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: false }

    // Simpan Dark Mode
    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences -> preferences[DARK_MODE_KEY] = isDark }
    }

    // --- TAMBAHAN BARU ---

    // Baca Status Onboarding
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ONBOARDING_KEY] ?: false }

    // Simpan Status Onboarding (Selesai)
    suspend fun saveOnboardingCompleted() {
        context.dataStore.edit { preferences -> preferences[ONBOARDING_KEY] = true }
    }
}