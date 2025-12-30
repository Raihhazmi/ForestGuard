package com.forestguard.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Palet Warna Gelap
private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    secondary = ActionBlue,
    tertiary = ActionRed,
    background = DarkBackground,  // Background Hitam
    surface = DarkSurface,        // Kartu Abu Gelap
    onBackground = DarkText,      // Teks Putih
    onSurface = DarkText          // Teks Putih di atas kartu
)

// Palet Warna Terang
private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = ActionBlue,
    tertiary = ActionRed,
    background = LightBackground, // Background Abu Terang
    surface = LightSurface,       // Kartu Putih
    onBackground = LightText,     // Teks Hitam
    onSurface = LightText         // Teks Hitam di atas kartu
)

@Composable
fun ForestGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Ini menerima nilai dari MainActivity
    content: @Composable () -> Unit
) {
    // Pilih skema warna berdasarkan tombol Dark Mode
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Mengatur warna Status Bar (Jam/Baterai)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Status bar ikut warna background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan file Typography.kt ada (bawaan project)
        content = content
    )
}