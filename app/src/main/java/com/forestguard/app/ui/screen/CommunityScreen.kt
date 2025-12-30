package com.forestguard.app.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.forestguard.app.data.ReportRepository
import com.forestguard.app.model.Comment
import com.forestguard.app.model.Report
import com.forestguard.app.ui.theme.ForestGreen
import kotlinx.coroutines.launch

// DEFINISI WARNA LOKAL
private val FabRed = Color(0xFFFF4C4C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    val reports = viewModel.reports.value
    val isLoading = viewModel.isLoading.value
    val selectedFilter = viewModel.selectedFilter.value
    val currentUserId = viewModel.currentUserId.value
    val repository = remember { ReportRepository() }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val activeComments = viewModel.activeComments.value
    val scope = rememberCoroutineScope()

    Scaffold(
        // NAVBAR - Gunakan Surface color agar ikut tema
        bottomBar = {
            CommunityBottomBar(onNavigateToHome, onNavigateToMap, onNavigateToProfile)
        },
        // FAB KAMERA MERAH
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = FabRed,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(64.dp).offset(y = 40.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Lapor", modifier = Modifier.size(30.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background // Background ikut tema (Hitam/Putih)
    ) { padding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ForestGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header Stats
                item { CommunityStatsHeader(viewModel) }

                // Filter Section
                item { CommunityFilterSection(selectedFilter, viewModel::setFilter) }

                // Feed List
                if (reports.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada postingan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(reports) { report ->
                        SocialPostCard(
                            report = report,
                            repository = repository,
                            currentUserId = currentUserId,
                            onLikeClick = { viewModel.toggleLike(report.id) },
                            onCommentClick = {
                                viewModel.openComments(report.id)
                                showBottomSheet = true
                            },
                            onActionClick = { action ->
                                when(action) {
                                    "Verify" -> viewModel.verifyReport(report.id, true)
                                    "Delete" -> viewModel.deleteReport(report.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // --- SHEET KOMENTAR ---
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.closeComments()
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface // Surface color untuk BottomSheet
            ) {
                CommentSheetContent(
                    comments = activeComments,
                    repository = repository,
                    onSendComment = { text -> viewModel.sendComment(text) }
                )
            }
        }
    }
}

// --- NAVBAR ---
@Composable
fun CommunityBottomBar(onHome: () -> Unit, onMap: () -> Unit, onProfile: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // Surface color
        tonalElevation = 10.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home", fontSize = 10.sp) },
            selected = false,
            onClick = onHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Map, null) },
            label = { Text("Peta", fontSize = 10.sp) },
            selected = false,
            onClick = onMap
        )
        NavigationBarItem(
            icon = {},
            label = {},
            selected = false,
            onClick = {},
            enabled = false
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Forum, null) },
            label = { Text("Forum", fontSize = 10.sp) },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ForestGreen,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer // Warna indikator adaptif
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profil", fontSize = 10.sp) },
            selected = false,
            onClick = onProfile
        )
    }
}

// --- SHEET KOMENTAR ---
@Composable
fun CommentSheetContent(comments: List<Comment>, repository: ReportRepository, onSendComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().height(500.dp).padding(16.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.height(16.dp))

        Text("Komentar", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onSurface)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (comments.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada komentar.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {
                items(comments) { comment ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (!comment.avatarId.isNullOrEmpty()) {
                            AsyncImage(
                                model = repository.getImageUrl(comment.avatarId),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Text(comment.userName.take(1), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(comment.createdAt.take(10), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(comment.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)

                            Text(
                                "Balas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { text = "@${comment.userName} " }
                            )
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
        ) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Tambahkan komentar...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3
            )
            IconButton(
                onClick = { if (text.isNotEmpty()) { onSendComment(text); text = "" } },
                enabled = text.isNotEmpty()
            ) {
                Icon(Icons.Default.Send, null, tint = if (text.isNotEmpty()) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// --- SOCIAL POST CARD ---
@Composable
fun SocialPostCard(
    report: Report,
    repository: ReportRepository,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onActionClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isLiked = report.likedBy.contains(currentUserId)
    val likeCount = report.likedBy.size
    val isVerified = report.status == "Terverifikasi"
    val commentCount = report.commentCount
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Card ikut tema
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(report.userId.take(1).uppercase(), fontWeight = FontWeight.Bold, color = ForestGreen)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Relawan ${report.userId.take(4)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                        }
                    }
                    Text("📍 Lokasi Terpantau • ${report.createdAt.take(10)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(text = { Text("✅ Verifikasi", color = MaterialTheme.colorScheme.onSurface) }, onClick = { onActionClick("Verify"); showMenu = false })
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        DropdownMenuItem(text = { Text("🗑️ Hapus", color = FabRed) }, onClick = { onActionClick("Delete"); showMenu = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(report.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))

            // Image
            AsyncImage(
                model = repository.getImageUrl(report.imageId),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Divider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Footer
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                SocialActionButton(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "$likeCount Suka",
                    color = if (isLiked) FabRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onLikeClick
                )
                SocialActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "$commentCount Komentar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onCommentClick
                )
                SocialActionButton(Icons.Outlined.Share, "Bagikan", MaterialTheme.colorScheme.onSurfaceVariant) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Info Hutan: ${report.description}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share"))
                }
            }
        }
    }
}

// --- Header Stats & Filter (tetap pakai ForestGreen yang solid, jadi aman di dark mode) ---
@Composable
fun CommunityStatsHeader(viewModel: CommunityViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = ForestGreen), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Pusat Komunitas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatInfoItem(viewModel.statUserCount.value, "Relawan Aktif", Icons.Default.Groups)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                StatInfoItem(viewModel.statDiscussionCount.value, "Total Diskusi", Icons.Default.Chat)
            }
        }
    }
}

@Composable
fun StatInfoItem(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun SocialActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }.padding(4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFilterSection(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val filters = listOf("Terbaru", "Terverifikasi", "Populer")
        items(filters) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreen, selectedLabelColor = Color.White),
                border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outline)
            )
        }
    }
}