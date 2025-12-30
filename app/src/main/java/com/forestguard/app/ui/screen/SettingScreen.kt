package com.forestguard.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestguard.app.ui.theme.ForestGreen // Pastikan ForestGreen ada di ui/theme/Color.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    viewModel: SettingViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Ambil status Dark Mode dari DataStore
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Ambil status fitur lain (masih state lokal di ViewModel)
    val isPushEnabled = viewModel.isPushEnabled.value
    val isAutoSync = viewModel.isAutoSync.value
    val isUploadCellular = viewModel.isUploadCellular.value
    val isBackgroundLocation = viewModel.isBackgroundLocation.value
    val isMLOnDevice = viewModel.isMLOnDevice.value
    val isDebugMode = viewModel.isDebugMode.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        // PERBAIKAN: Gunakan background dari Tema (Otomatis Hitam saat Dark Mode)
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            SectionHeader("Tampilan & Bahasa")
            SettingSwitch(
                icon = Icons.Outlined.DarkMode,
                title = "Mode Gelap",
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
            SettingAction(Icons.Outlined.Language, "Bahasa", "Indonesia") {}

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Notifikasi")
            SettingSwitch(
                icon = Icons.Outlined.Notifications,
                title = "Push Notification",
                checked = isPushEnabled,
                onCheckedChange = { viewModel.setPushEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Data & Privasi")
            SettingSwitch(
                icon = Icons.Outlined.Sync,
                title = "Sinkronisasi Otomatis",
                subtitle = "Upload laporan saat online",
                checked = isAutoSync,
                onCheckedChange = { viewModel.toggleAutoSync(it, context) }
            )
            SettingSwitch(
                icon = Icons.Outlined.SignalCellularAlt,
                title = "Upload via Seluler",
                checked = isUploadCellular,
                onCheckedChange = { viewModel.setUploadCellular(it) }
            )
            SettingSwitch(
                icon = Icons.Outlined.LocationOn,
                title = "Lokasi Latar Belakang",
                checked = isBackgroundLocation,
                onCheckedChange = { viewModel.setBackgroundLocation(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Peta & Penyimpanan")
            SettingAction(Icons.Outlined.Map, "Unduh Peta Offline", "Kelola area") { viewModel.downloadOfflineMaps(context) }
            SettingAction(Icons.Outlined.DeleteOutline, "Hapus Cache", "Kosongkan ruang") { viewModel.clearCache(context) }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Sistem")
            SettingSwitch(
                icon = Icons.Outlined.SmartToy,
                title = "AI Smoke Detection",
                checked = isMLOnDevice,
                onCheckedChange = { viewModel.setMLOnDevice(it) }
            )
            SettingSwitch(
                icon = Icons.Outlined.BugReport,
                title = "Debug Mode Admin",
                checked = isDebugMode,
                onCheckedChange = { viewModel.setDebugMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("ForestGuard v1.0", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestGreen, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
}

@Composable
fun SettingSwitch(icon: ImageVector, title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // PERBAIKAN: Gunakan Surface dari Tema
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ForestGreen))
        }
    }
}

@Composable
fun SettingAction(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    // PERBAIKAN: Gunakan Surface dari Tema
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}