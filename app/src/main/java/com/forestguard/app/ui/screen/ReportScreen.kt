package com.forestguard.app.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.ui.theme.ForestGreen // Pastikan ForestGreen ada di ui/theme/Color.kt
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- WARNA TAMBAHAN ---
val ReportRed = Color(0xFFFF4C4C)
val ReportYellow = Color(0xFFFFC107)
val ReportGray = Color(0xFFF5F5F5)
val TextGray = Color(0xFF757575)

@Composable
fun ReportScreen(
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ReportRepository() }

    // --- STATE DATA LAPORAN ---
    var selectedType by remember { mutableStateOf("Titik Api") }
    var selectedSeverity by remember { mutableStateOf("Sedang") }
    var description by remember { mutableStateOf("") }
    // Default GPS (Simulasi)
    var locationText by remember { mutableStateOf("-2.548926, 118.014864") }

    // Checkbox State
    var isSmokeVisible by remember { mutableStateOf(false) }
    var isFireSpread by remember { mutableStateOf(false) }
    var isNearSettlement by remember { mutableStateOf(false) }

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Launcher Kamera
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) capturedImageUri = currentPhotoUri
    }

    // Launcher Izin
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Butuh izin kamera!", Toast.LENGTH_SHORT).show()
    }

    // Fungsi Buka Kamera (Di dalam Composable agar bisa akses Context)
    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Panggil fungsi helper yang ada di bawah
            val file = context.createImageFile()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            currentPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(ForestGreen)
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buat Laporan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("Bantu pantau hutan Indonesia", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(start = 48.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terhubung", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                            Text("Aman", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {

                // --- 2. JENIS LAPORAN ---
                Text("Jenis Laporan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportTypeCard("Titik Api", Icons.Default.LocalFireDepartment, selectedType == "Titik Api", ReportRed) { selectedType = "Titik Api" }
                    ReportTypeCard("Kerusakan", Icons.Default.Forest, selectedType == "Kerusakan", Color.Gray) { selectedType = "Kerusakan" }
                    ReportTypeCard("Satwa", Icons.Default.Pets, selectedType == "Satwa", Color.Gray) { selectedType = "Satwa" }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. TINGKAT KEPARAHAN ---
                Text("Tingkat Keparahan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SeverityChip("Rendah", selectedSeverity == "Rendah") { selectedSeverity = "Rendah" }
                    SeverityChip("Sedang", selectedSeverity == "Sedang") { selectedSeverity = "Sedang" }
                    SeverityChip("Tinggi", selectedSeverity == "Tinggi") { selectedSeverity = "Tinggi" }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. LOKASI ---
                Text("Lokasi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("GPS Coordinates", fontSize = 12.sp, color = TextGray)
                                Text(locationText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { locationText = "-2.548926, 118.014864 (Updated)"; Toast.makeText(context, "Lokasi Diperbarui", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Dapatkan Lokasi Otomatis") }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 5. DESKRIPSI ---
                Text("Deskripsi Kejadian", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Jelaskan apa yang Anda lihat...", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 6. FOTO BUKTI ---
                Text("Foto Bukti", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                MediaUploadBox(Icons.Default.CameraAlt, "Ambil Foto", capturedImageUri != null, capturedImageUri, { openCamera() }, Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(24.dp))

                // --- 7. CHECKBOX ---
                Text("Informasi Tambahan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                CustomCheckbox("Asap tebal terlihat", isSmokeVisible) { isSmokeVisible = it }
                CustomCheckbox("Api menyebar cepat", isFireSpread) { isFireSpread = it }
                CustomCheckbox("Dekat pemukiman", isNearSettlement) { isNearSettlement = it }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 8. TOMBOL KIRIM ---
                Button(
                    onClick = {
                        if (capturedImageUri == null) { Toast.makeText(context, "Foto wajib ada!", Toast.LENGTH_SHORT).show(); return@Button }

                        // Bersihkan teks lokasi
                        val cleanLoc = locationText.replace("(Updated)", "").trim()
                        val locParts = cleanLoc.split(",").map { it.trim() }

                        if (locParts.size != 2) { Toast.makeText(context, "Lokasi GPS tidak valid!", Toast.LENGTH_SHORT).show(); return@Button }

                        isLoading = true
                        val lat = locParts[0].toDoubleOrNull() ?: 0.0
                        val lon = locParts[1].toDoubleOrNull() ?: 0.0

                        val severityInt = when(selectedSeverity) { "Rendah" -> 1; "Sedang" -> 3; "Tinggi" -> 5; else -> 1 }

                        scope.launch {
                            repository.uploadReport(
                                context = context,
                                imageUri = capturedImageUri!!,
                                description = "$selectedType - $description",
                                severity = severityInt,
                                latitude = lat,
                                longitude = lon,
                                onSuccess = { isLoading = false; Toast.makeText(context, "Laporan Terkirim!", Toast.LENGTH_LONG).show(); onSubmitClick() },
                                onError = { msg -> isLoading = false; Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else { Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Kirim Laporan", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ================= KOMPONEN UI TAMBAHAN =================

@Composable
fun ReportTypeCard(text: String, icon: ImageVector, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, activeColor) else BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) activeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(100.dp, 80.dp).clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isSelected) activeColor else Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) activeColor else Color.Gray)
        }
    }
}

@Composable
fun SeverityChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) {
        when (text) { "Rendah" -> Color(0xFFE8F5E9); "Sedang" -> Color(0xFFFFF8E1); "Tinggi" -> Color(0xFFFFEBEE); else -> Color.Gray }
    } else MaterialTheme.colorScheme.surface

    val borderColor = if (isSelected) {
        when (text) { "Rendah" -> ForestGreen; "Sedang" -> ReportYellow; "Tinggi" -> ReportRed; else -> Color.Gray }
    } else Color.LightGray

    Surface(shape = RoundedCornerShape(50), border = BorderStroke(1.dp, borderColor), color = backgroundColor, modifier = Modifier.clickable { onClick() }.width(100.dp).height(36.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.Black else Color.Gray) }
    }
}

@Composable
fun MediaUploadBox(icon: ImageVector, label: String, hasImage: Boolean, imageUri: Uri? = null, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.height(150.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick() }, contentAlignment = Alignment.Center) {
        if (hasImage && imageUri != null) { AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        else { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = TextGray, modifier = Modifier.size(32.dp)); Spacer(modifier = Modifier.height(4.dp)); Text(label, fontSize = 12.sp, color = TextGray) } }
    }
}

@Composable
fun CustomCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp)) {
        Checkbox(checked = checked, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = ForestGreen))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

// --- FUNGSI HELPER (Ditaruh di luar Composable) ---
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", externalCacheDir)
}