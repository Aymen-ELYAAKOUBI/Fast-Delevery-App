package com.example.suiverliverson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun StatsScreen() {
    val s = LocalStrings.current
    var reviewsList by remember { mutableStateOf(initialReviewsList) }

    // محاكاة حية لتلقي التقييمات
    LaunchedEffect(Unit) {
        while (true) {
            delay(12_000L)
            val newRev = generateRandomReview()
            reviewsList = listOf(newRev) + reviewsList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // العنوان مترجم
        Text(
            text = s.stats,
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // كارد التقييم الإجمالي مترجم
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "4.8",
                        color = White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        repeat(5) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = s.reviewsCount.format(reviewsList.size + 120),
                        color = LightGray,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    RatingBarItem(stars = "5★", progress = 0.85f)
                    RatingBarItem(stars = "4★", progress = 0.10f)
                    RatingBarItem(stars = "3★", progress = 0.03f)
                    RatingBarItem(stars = "2★", progress = 0.01f)
                    RatingBarItem(stars = "1★", progress = 0.01f)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // الإنفوجرافيك الأسبوعي مترجم
        Text(
            text = s.weeklyInfographic,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = s.weeklyPerformance,
                    color = LightGray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val heights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.85f, 1.0f, 0.6f)

                    days.zip(heights).forEach { (day, height) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height((100 * height).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (height >= 0.9f) Red else Green)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(day, color = LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // التقييمات والتعليقات مترجمة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.customerReviews,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
                Spacer(Modifier.width(6.dp))
                Text(s.liveBadge, color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))

        reviewsList.forEach { review ->
            ReviewCard(review)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RatingBarItem(stars: String, progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(stars, color = LightGray, fontSize = 11.sp, modifier = Modifier.width(24.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFFB300))
            )
        }
    }
}

@Composable
private fun ReviewCard(review: CustomerReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarColor = when (review.avatarType) {
                        AvatarType.WOMAN -> Color(0xFFE91E63)
                        AvatarType.MAN -> Color(0xFF1976D2)
                        AvatarType.OLD_MAN -> Color(0xFF8D6E63)
                        AvatarType.YOUNG -> Color(0xFF4CAF50)
                    }
                    val avatarEmoji = when (review.avatarType) {
                        AvatarType.WOMAN -> "👩‍🦰"
                        AvatarType.MAN -> "👨"
                        AvatarType.OLD_MAN -> "👴"
                        AvatarType.YOUNG -> "👦"
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                            .border(1.dp, White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(review.customerName, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(review.timeAgo, color = LightGray, fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("%.1f".format(review.rating), color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment,
                color = LightGray,
                fontSize = 13.sp
            )
        }
    }
}