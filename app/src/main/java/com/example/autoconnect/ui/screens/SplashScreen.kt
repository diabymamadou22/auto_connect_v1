package com.example.autoconnect.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autoconnect.R
import com.example.autoconnect.ui.theme.BluePrimary
import kotlinx.coroutines.delay

/**
 * Splash Screen displaying the official Auto Connect Mali logo for 3 seconds
 * before transitioning to the first screen of the application.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val pulse = remember { Animatable(1f) }

    // Animation and 3-second timer
    LaunchedEffect(Unit) {
        // Entrance animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        // Subtle breathing pulse
        pulse.animateTo(
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Exact 3 seconds wait before navigating
    LaunchedEffect(Unit) {
        delay(3000L)
        onSplashFinished()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF)
                    )
                )
            )
            .testTag("splash_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        val isCompactHeight = maxHeight < 640.dp
        val logoBoxSize = if (isCompactHeight) 140.dp else 200.dp
        val haloSize = if (isCompactHeight) 170.dp else 230.dp
        val bottomPadding = if (isCompactHeight) 16.dp else 36.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Main Logo with gentle scale and drop shadow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale.value * pulse.value)
                    .alpha(alpha.value)
            ) {
                // Background soft halo
                Box(
                    modifier = Modifier
                        .size(haloSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    BluePrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(logoBoxSize)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Logo Auto Connect Mali",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .testTag("splash_app_logo")
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 28.dp))

            // App Name & Slogan with Fade-in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alpha.value)
            ) {
                Text(
                    text = "AUTO CONNECT MALI",
                    fontSize = if (isCompactHeight) 19.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Votre assistant auto nouvelle génération",
                    fontSize = if (isCompactHeight) 12.sp else 13.5.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mali National Flag Accent Bar (Vert, Jaune, Rouge)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .height(4.dp)
                        .width(68.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0xFF14B8A6)) // Green
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0xFFEAB308)) // Yellow / Gold
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0xFFEF4444)) // Red
                    )
                }
            }
        }

        // Bottom Loading Indicator
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = BluePrimary,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chargement en cours...",
                fontSize = 11.5.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
