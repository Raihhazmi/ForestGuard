package com.forestguard.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi // Add this import
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forestguard.app.ui.theme.ForestGreen
import kotlinx.coroutines.launch

// Data Class untuk isi halaman
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class) // Add this annotation
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    // Definisi 3 Halaman
    val pages = listOf(
        OnboardingPage(
            "Pantau Hutan Real-time",
            "Dapatkan informasi titik api dan kondisi hutan terkini langsung dari peta interaktif.",
            Icons.Outlined.Forest
        ),
        OnboardingPage(
            "Lapor Cepat & Akurat",
            "Kirim laporan kejadian dengan foto dan lokasi GPS otomatis untuk penanganan segera.",
            Icons.Outlined.ReportProblem
        ),
        OnboardingPage(
            "Komunitas Relawan",
            "Bergabung dengan ribuan relawan, berdiskusi, dan beraksi bersama menjaga alam.",
            Icons.Outlined.Groups
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tombol Skip (Pojok Kanan Atas)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = {
                    viewModel.completeOnboarding()
                    onFinish()
                }) {
                    Text("Lewati", color = Color.Gray)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp)) // Spacer biar tinggi konsisten
            }
        }

        // Pager (Konten Utama)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        // Indikator & Tombol Navigasi Bawah
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Indikator Titik-titik
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) ForestGreen else Color.LightGray
                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Tombol Next / Get Started
            Box {
                // Tombol Next (Panah)
                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                } else {
                    // Tombol Get Started (Mulai)
                    Button(
                        onClick = {
                            viewModel.completeOnboarding()
                            onFinish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Mulai Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ilustrasi (Menggunakan Icon Vector Besar)
        // Nanti bisa diganti Image(painter = painterResource(id = R.drawable.gambar_1)...)
        Box(
            modifier = Modifier
                .size(250.dp)
                .background(ForestGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = ForestGreen
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 22.sp
        )
    }
}