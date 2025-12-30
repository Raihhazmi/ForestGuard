package com.forestguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestguard.app.data.AppwriteClient
import com.forestguard.app.ui.screen.ForestGuardApp
import com.forestguard.app.ui.screen.SettingViewModel
import com.forestguard.app.ui.theme.ForestGuardTheme // Pastikan import Theme ini benar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppwriteClient.init(applicationContext)

        setContent {
            // 1. Panggil SettingViewModel di Root (Akar)
            val settingViewModel: SettingViewModel = viewModel()

            // 2. Pantau status Dark Mode dari DataStore
            val isDarkMode by settingViewModel.isDarkMode.collectAsState()

            // 3. Terapkan Tema ke SELURUH APLIKASI
            ForestGuardTheme(
                darkTheme = isDarkMode // <-- INI KUNCINYA
            ) {
                // 4. Masuk ke Navigasi
                ForestGuardApp(settingViewModel = settingViewModel)
            }
        }
    }
}