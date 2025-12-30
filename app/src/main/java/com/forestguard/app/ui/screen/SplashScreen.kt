package com.forestguard.app.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestguard.app.R
import com.forestguard.app.ui.theme.ForestGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) ForestGreen else Color.White
    val textColor = if (isDark) Color.White else ForestGreen
    val logoRes = if (isDark) R.drawable.logo_forest_light else R.drawable.logo_forest_dark

    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(5000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .padding(bottom = 60.dp) // 🔥 Inilah yang mengangkat posisi tulisan sedikit ke atas
        ) {

            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "ForestGuard Logo",
                modifier = Modifier
                    .size(250.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FOREST GUARD",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "JAGALAH HUTAN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                letterSpacing = 4.sp
            )
        }
    }
}
