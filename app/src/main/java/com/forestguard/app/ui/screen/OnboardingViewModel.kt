package com.forestguard.app.ui.screen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.forestguard.app.data.UserPreferences
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.saveOnboardingCompleted()
        }
    }
}