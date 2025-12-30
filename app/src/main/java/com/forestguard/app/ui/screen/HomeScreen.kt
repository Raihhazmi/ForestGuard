package com.forestguard.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.model.Report

// Warna Brand Tetap
val ForestGreen = Color(0xFF008F45)
val ActionRed = Color(0xFFFF4C4C)
val ActionBlue = Color(0xFF448AFF)
val ActionPurple = Color(0xFF7C4DFF)
val ActionOrange = Color(0xFFFF9800)

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadData() }

    val reports = viewModel.reportList.value
    val totalReports = viewModel.totalReports.value
    val userName = viewModel.userName.value

    Scaffold(
        // PERBAIKAN: Background mengikuti tema
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = ActionRed,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(64.dp).offset(y = 40.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Lapor", modifier = Modifier.size(30.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = { BottomNavigationBar(onNavigateToMap, onNavigateToCommunity, onNavigateToProfile) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HeaderSection(userName, totalReports) }
            item { QuickActionsSection(onNavigateToReport, onNavigateToMap) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Laporan Terkini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Refresh", color = ForestGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { viewModel.loadData() })
                }
            }
            if (reports.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Belum ada laporan masuk.", color = Color.Gray) } }
            } else {
                items(reports) { report ->
                    AlertCardReal(report)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, totalLaporan: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(ForestGreen)
            .padding(start = 24.dp, end = 24.dp, top = 60.dp, bottom = 32.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Halo, ${userName.take(10)}..", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Status Hutan: AMAN", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                }
                Row { IconButton(onClick = {}) { Icon(Icons.Outlined.Notifications, null, tint = Color.White) } }
            }
            Spacer(modifier = Modifier.height(24.dp))
            StatCard(Icons.Default.Assignment, "$totalLaporan", "Total Laporan Masuk", ActionBlue, Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.Forest, "1,245", "Hektar Hutan", ForestGreen, Modifier.weight(1f))
                StatCard(Icons.Default.Shield, "82", "Ranger Aktif", ActionPurple, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier) {
    // PERBAIKAN: Gunakan Surface Tema
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = label, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun QuickActionsSection(onNavigateToReport: () -> Unit, onNavigateToMap: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val linkPanduan = "https://toolsfortransformation.net/wp-content/uploads/2017/05/Panduan_Pengendalian_Kebakaran_Hutan_dan.pdf"
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Aksi Cepat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard("Lapor Api", Icons.Default.LocalFireDepartment, ActionRed, onNavigateToReport, Modifier.weight(1f))
            ActionCard("Peta", Icons.Default.Map, ActionBlue, onNavigateToMap, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        ActionCard("Panduan Keselamatan & Pengendalian", Icons.Default.MenuBook, ActionOrange, { uriHandler.openUri(linkPanduan) }, Modifier.fillMaxWidth())
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = modifier.height(85.dp).clickable { onClick() }) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AlertCardReal(report: Report) {
    val repo = ReportRepository()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(model = repo.getImageUrl(report.imageId), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = if(report.severity >= 3) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                        Text(text = if(report.severity >= 3) "BAHAYA" else "Info", color = if(report.severity >= 3) ActionRed else ForestGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(report.createdAt.take(10), color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = report.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Oleh: ${report.userId.take(5)}...", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(onNavigateToMap: () -> Unit, onNavigateToCommunity: () -> Unit, onNavigateToProfile: () -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 10.dp) {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, selected = true, onClick = {}, colors = NavigationBarItemDefaults.colors(selectedIconColor = ForestGreen, indicatorColor = Color(0xFFE8F5E9)))
        NavigationBarItem(icon = { Icon(Icons.Outlined.Map, null) }, label = { Text("Peta", fontSize = 10.sp) }, selected = false, onClick = onNavigateToMap)
        NavigationBarItem(icon = {}, label = {}, selected = false, onClick = {}, enabled = false)
        NavigationBarItem(icon = { Icon(Icons.Outlined.Forum, null) }, label = { Text("Forum", fontSize = 10.sp) }, selected = false, onClick = onNavigateToCommunity)
        NavigationBarItem(icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("Profil", fontSize = 10.sp) }, selected = false, onClick = onNavigateToProfile)
    }
}