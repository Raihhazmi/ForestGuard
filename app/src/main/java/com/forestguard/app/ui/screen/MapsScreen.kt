package com.forestguard.app.ui.screen

import android.Manifest
import android.content.Context
// --- IMPORT PENTING YANG TADI HILANG ---
import android.content.Intent
import android.net.Uri
// ---------------------------------------
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestguard.app.model.Forest
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Warna Desain
val MapGreen = Color(0xFF008F45)
val MapRed = Color(0xFFFF4C4C)
val MapBlue = Color(0xFF448AFF)
val MapGray = Color(0xFFF8F9FA)

@Composable
fun MapsScreen(
    viewModel: MapsViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val reports = viewModel.reports.value
    val forests = viewModel.forests.value
    val activeFilter = viewModel.activeFilter.value

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        if (!hasLocationPermission) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        bottomBar = {
            MapsBottomBar(onHomeClick = onNavigateToHome, onReportClick = onNavigateToReport, onMapClick = onNavigateToMap, onCommunityClick = onNavigateToCommunity, onProfileClick = onNavigateToProfile)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = MapRed,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(64.dp).offset(y = 40.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Lapor", modifier = Modifier.size(30.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        // PERBAIKAN: Gunakan background tema
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(4.5)
                        controller.setCenter(GeoPoint(-2.5, 118.0))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    if (hasLocationPermission) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                        locationOverlay.enableMyLocation()
                        val personBitmap = createBlueDotBitmap()
                        locationOverlay.setPersonIcon(personBitmap)
                        locationOverlay.setDirectionIcon(personBitmap)
                        mapView.overlays.add(locationOverlay)
                    }
                    forests.forEach { forest ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(forest.lat, forest.lon)
                        marker.title = forest.name
                        marker.snippet = "Status: ${forest.status}"
                        marker.icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)
                        mapView.overlays.add(marker)
                    }
                    reports.forEach { report ->
                        val lat = -1.5 + (Math.random() - 0.5) * 10
                        val lon = 118.0 + (Math.random() - 0.5) * 20
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(lat, lon)
                        marker.title = "Laporan Warga"
                        marker.snippet = report.description
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderMapSection(activeFilter, viewModel::setFilter)
                Spacer(modifier = Modifier.weight(1f))
                BottomMapInfoSection(reports.size, forests)
            }
        }
    }
}

// --- FUNGSI UTILS ---
fun openGoogleMaps(context: Context, lat: Double, lon: Double, label: String) {
    val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($label)")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon"))
        context.startActivity(browserIntent)
    }
}

fun createBlueDotBitmap(): Bitmap {
    val size = 60
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = android.graphics.Color.parseColor("#4285F4")
    paint.alpha = 70
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.color = android.graphics.Color.parseColor("#4285F4")
    paint.alpha = 255
    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 4f, paint)
    return bitmap
}

// --- KOMPONEN UI ---

@Composable
fun HeaderMapSection(activeFilter: String, onFilterSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(MapGreen).padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Peta Interaktif", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Real-time monitoring", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.FilterAlt, null, tint = Color.White) } }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MapFilterChip("Tutupan Hutan", Icons.Default.Forest, activeFilter == "Hutan") { onFilterSelected("Hutan") }
            MapFilterChip("Laporan Warga", Icons.Default.Campaign, activeFilter == "Laporan") { onFilterSelected("Laporan") }
        }
    }
}

@Composable
fun MapFilterChip(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f), contentColor = if (isSelected) MapGreen else Color.White, shape = RoundedCornerShape(50), modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BottomMapInfoSection(reportCount: Int, forests: List<Forest>) {
    val context = LocalContext.current
    // PERBAIKAN: Gunakan Surface dari Tema agar Dark Mode aman
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$reportCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MapBlue)
                    Text("Laporan", fontSize = 10.sp, color = Color.Gray)
                }
                Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${forests.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MapGreen)
                    Text("Hutan Pantau", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Text color otomatis hitam/putih sesuai tema
            Text("Lokasi Hutan Terdekat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.height(150.dp).verticalScroll(rememberScrollState())) {
                Column {
                    forests.forEach { forest ->
                        FavoriteLocationItem(name = forest.name, status = forest.status, isAlert = forest.isAlert, onViewClick = { openGoogleMaps(context, forest.lat, forest.lon, forest.name) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (forests.isEmpty()) { Text("Memuat data hutan...", color = Color.Gray, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
fun FavoriteLocationItem(name: String, status: String, isAlert: Boolean, onViewClick: () -> Unit) {
    // PERBAIKAN: SurfaceVariant agar kontras di Dark Mode
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Forest, null, tint = MapGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Text color otomatis hitam/putih
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(status, fontSize = 11.sp, color = if (isAlert) MapRed else MapGreen)
            }
            Text("Lihat", fontSize = 12.sp, color = MapGreen, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onViewClick() })
        }
    }
}

@Composable
fun MapsBottomBar(onHomeClick: () -> Unit, onReportClick: () -> Unit, onMapClick: () -> Unit, onCommunityClick: () -> Unit, onProfileClick: () -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 10.dp) {
        NavigationBarItem(icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, selected = false, onClick = onHomeClick)
        NavigationBarItem(icon = { Icon(Icons.Default.Map, null) }, label = { Text("Peta", fontSize = 10.sp) }, selected = true, onClick = onMapClick, colors = NavigationBarItemDefaults.colors(selectedIconColor = MapGreen, indicatorColor = Color(0xFFE8F5E9)))
        NavigationBarItem(icon = {}, label = {}, selected = false, onClick = {}, enabled = false)
        NavigationBarItem(icon = { Icon(Icons.Outlined.Forum, null) }, label = { Text("Forum", fontSize = 10.sp) }, selected = false, onClick = onCommunityClick)
        NavigationBarItem(icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("Profil", fontSize = 10.sp) }, selected = false, onClick = onProfileClick)
    }
}