package com.example.suiverliverson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.suiverliverson.ui.theme.AppBackground
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun LiversonLogo(iconSize: Dp = 110.dp, textSize: TextUnit = 44.sp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .height(iconSize)
                .offset(x = -iconSize * 0.25f)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = White)) { append("Liver") }
                withStyle(SpanStyle(color = Red)) { append("son") }
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
            .background(AppBackground)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (i in 0..9) {
                val y = h * i / 9f
                drawLine(
                    Color.White.copy(alpha = 0.04f),
                    Offset(0f, y),
                    Offset(w, y - 160f),
                    strokeWidth = 2f
                )
            }
            for (i in 0..5) {
                val x = w * i / 5f
                drawLine(
                    Color.White.copy(alpha = 0.03f),
                    Offset(x, 0f),
                    Offset(x + 120f, h),
                    strokeWidth = 2f
                )
            }

            val wave = Path().apply {
                moveTo(0f, h * 0.90f)
                cubicTo(w * 0.30f, h * 0.80f, w * 0.55f, h * 0.97f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                wave,
                Brush.horizontalGradient(listOf(Red, Color(0xFFE8304F)))
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiversonLogo(iconSize = 150.dp, textSize = 52.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = LocalStrings.current.slogan.replace(". ", ".\n"),
                color = LightGray,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .width(110.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(White.copy(alpha = 0.35f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(110.dp * progress.value)
                    .background(White)
            )
        }
    }
}