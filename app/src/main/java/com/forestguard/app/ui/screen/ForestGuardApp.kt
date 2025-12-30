package com.forestguard.app.ui.screen

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.forestguard.app.data.AuthRepository
import com.forestguard.app.data.UserPreferences // Import ini
import androidx.compose.ui.platform.LocalContext // Import ini
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ForestGuardApp(
    settingViewModel: SettingViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { AuthRepository() }
    val userPreferences = remember { UserPreferences(context) } // Inisialisasi Prefs

    // State untuk menentukan start destination
    var startDestination by remember { mutableStateOf("splash") }
    var isChecking by remember { mutableStateOf(true) }

    // LOGIKA PENGECEKAN NAVIGASI AWAL
    LaunchedEffect(Unit) {
        // 1. Tahan splash screen sebentar (opsional, sudah dihandle screen splash)

        // 2. Cek apakah onboarding sudah pernah dibuka
        val onboardingCompleted = userPreferences.isOnboardingCompleted.first()

        if (!onboardingCompleted) {
            // Kalau belum pernah onboarding -> Ke Onboarding
            startDestination = "onboarding"
        } else {
            // Kalau sudah onboarding -> Cek Login
            val user = repository.getUser()
            startDestination = if (user != null) "home" else "login"
        }
        isChecking = false
    }

    if (isChecking) {
        // Tampilkan layar kosong atau splash manual sementara loading logic
        // Tapi karena kita punya route "splash", kita biarkan NavHost menangani
        // Untuk amannya, return kosong dulu biar tidak blink
        return
    }

    NavHost(navController = navController, startDestination = "splash") {

        // 0. SPLASH SCREEN
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    // Saat splash selesai animasi, pindah ke tujuan hasil cek di atas
                    navController.navigate(startDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 0.5. ONBOARDING (Route Baru)
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    // Setelah onboarding selesai -> Masuk Login
                    // Dan hapus onboarding dari backstack agar tidak bisa kembali
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 1. LOGIN
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // 2. REGISTER
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // 3. HOME (DASHBOARD)
        composable("home") {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") { popUpTo("home") { inclusive = true } }
                },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToMap = { navController.navigate("maps") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        // 4. LAPOR (CAMERA)
        composable("report") {
            ReportScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.popBackStack() }
            )
        }

        // 5. COMMUNITY (FEED)
        composable("community") {
            CommunityScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToMap = { navController.navigate("maps") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        // 6. MAPS SCREEN
        composable("maps") {
            MapsScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToMap = { },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        // 7. PROFILE SCREEN
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToMap = { navController.navigate("maps") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // 8. SETTING SCREEN
        composable("settings") {
            SettingScreen(
                onBack = { navController.popBackStack() },
                viewModel = settingViewModel
            )
        }
    }
}