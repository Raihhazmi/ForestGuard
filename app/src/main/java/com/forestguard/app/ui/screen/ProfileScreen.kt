package com.forestguard.app.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.forestguard.app.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val authRepo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    val userName = viewModel.userName.value
    val userEmail = viewModel.userEmail.value
    val avatarUrl = viewModel.avatarUrl.value
    val reportCount = viewModel.reportCount.value
    val isLoading = viewModel.isLoading.value

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPrivateMode by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) viewModel.updateAvatar(context, uri)
    }

    Scaffold(
        // Background Theme
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 10.dp) {
                NavigationBarItem(icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, selected = false, onClick = onNavigateToHome)
                NavigationBarItem(icon = { Icon(Icons.Outlined.Map, null) }, label = { Text("Peta", fontSize = 10.sp) }, selected = false, onClick = onNavigateToMap)
                NavigationBarItem(icon = {}, label = {}, selected = false, onClick = {}, enabled = false)
                NavigationBarItem(icon = { Icon(Icons.Outlined.Forum, null) }, label = { Text("Forum", fontSize = 10.sp) }, selected = false, onClick = onNavigateToCommunity)
                NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profil", fontSize = 10.sp) }, selected = true, onClick = {}, colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreen, indicatorColor = Color(0xFFE8F5E9)))
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToReport, containerColor = ActionRed, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(64.dp).offset(y = 40.dp)) { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(30.dp)) }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(260.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(ForestGreen))
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Icon(Icons.Outlined.Settings, "Pengaturan", tint = Color.White, modifier = Modifier.size(28.dp)) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 24.dp).fillMaxWidth().height(160.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8F5E9)).clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ForestGreen) }
                            else if (avatarUrl != null) { AsyncImage(model = avatarUrl, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                            else { Text(text = userName.take(1).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ForestGreen) }
                            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(20.dp).clip(CircleShape).background(ActionBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(userEmail, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(50)) {
                            Text("Level 5: Forest Guardian", fontSize = 11.sp, color = Color(0xFFFFA000), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem("Laporan", "$reportCount", Modifier.weight(1f))
                StatItem("Akurasi", "92%", Modifier.weight(1f))
                StatItem("Rank", "#127", Modifier.weight(1f))
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("XP Saat Ini", fontSize = 12.sp, color = Color.Gray)
                    Text("2450 / 3000 XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = 0.8f, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = ForestGreen, trackColor = Color(0xFFE0E0E0))
                Text("550 XP lagi untuk naik level!", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pengaturan & Aksi", modifier = Modifier.padding(horizontal = 24.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ProfileMenuItem(Icons.Outlined.Edit, "Edit Nama Profil") { newNameInput = userName; showEditNameDialog = true }
                ProfileSwitchItem(Icons.Outlined.VisibilityOff, "Anonimkan Laporan", isPrivateMode) { isPrivateMode = it }
                ProfileMenuItem(Icons.Outlined.FileDownload, "Ekspor Laporan (CSV)") { viewModel.exportToCSV(context) }
                ProfileMenuItem(Icons.Outlined.DeleteForever, "Hapus Akun", true) { showDeleteDialog = true }
                ProfileMenuItem(Icons.Default.Logout, "Keluar (Logout)", true) { scope.launch { authRepo.logout(); onLogout() } }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Ubah Nama") },
                text = { OutlinedTextField(value = newNameInput, onValueChange = { newNameInput = it }, label = { Text("Nama Baru") }, singleLine = true) },
                confirmButton = { TextButton(onClick = { viewModel.updateName(newNameInput, context); showEditNameDialog = false }) { Text("Simpan") } },
                dismissButton = { TextButton(onClick = { showEditNameDialog = false }) { Text("Batal") } }
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Akun?") },
                text = { Text("Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = { Button(onClick = { viewModel.deleteAccount(onSuccess = { onLogout() }, onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ActionRed)) { Text("Hapus Permanen") } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
            )
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isDestructive) ActionRed else ForestGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDestructive) ActionRed else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ProfileSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = ForestGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}