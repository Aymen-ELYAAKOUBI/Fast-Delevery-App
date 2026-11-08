package com.example.suiverliverson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun LiversonLogo(iconSize: Dp = 110.dp, textSize: TextUnit = 44.sp) {
    val isDarkTheme = LocalDarkMode.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(
                if (isDarkTheme) R.drawable.logo_dark_mode else R.drawable.logo_light_mode
            ),
            contentDescription = "Fast Delivery",
            modifier = Modifier.height(iconSize),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = if (isDarkTheme) White else Color(0xFF202124))) { append("Fast ") }
                withStyle(SpanStyle(color = Red)) { append("Delivery") }
            },
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1800))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (LocalDarkMode.current) Color(0xFF0C1421) else Color.White)
    ) {
        AnimatedVisibility(
            visible = true,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(650)) + scaleIn(initialScale = 0.88f, animationSpec = tween(650)) +
                    slideInVertically(initialOffsetY = { it / 12 }, animationSpec = tween(650))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LiversonLogo(iconSize = 150.dp, textSize = 52.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = LocalStrings.current.slogan.replace(". ", ".\n"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .width(110.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background((if (LocalDarkMode.current) White else Red).copy(alpha = 0.35f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(110.dp * progress.value)
                    .background(if (LocalDarkMode.current) White else Red)
            )
        }
    }
}