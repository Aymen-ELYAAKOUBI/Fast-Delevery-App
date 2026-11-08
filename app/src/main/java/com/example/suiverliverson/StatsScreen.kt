package com.example.suiverliverson

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.Red

@Composable
fun StatsScreen(deliveryManager: DeliveryManager) {
    val s = LocalStrings.current
    val cardColor = if (LocalDarkMode.current) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFF1F4F8)
    }
    val reviewsList = deliveryManager.customerReviewsList
    val reviewCount = reviewsList.size
    val averageRating = if (reviewCount == 0) 0.0 else reviewsList.map { it.rating }.average()
    val ratingDistribution = (5 downTo 1).associateWith { stars ->
        reviewsList.count { it.rating.toInt() == stars }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // العنوان مترجم
        Text(
            text = s.stats,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // كارد التقييم الإجمالي مترجم
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (reviewCount == 0) "—" else "%.1f".format(averageRating),
                        color = MaterialTheme.colorScheme.onSurface,
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
                        text = s.reviewsCount.format(reviewCount),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    (5 downTo 1).forEach { stars ->
                        val count = ratingDistribution.getValue(stars)
                        val progress = if (reviewCount == 0) 0f else count / reviewCount.toFloat()
                        RatingBarItem(stars = "$stars★", progress = progress)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // الإنفوجرافيك الأسبوعي مترجم
        Text(
            text = s.weeklyInfographic,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = s.weeklyPerformance,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            val animatedHeight by animateDpAsState(
                                targetValue = (100 * height).dp,
                                animationSpec = spring(dampingRatio = 0.72f, stiffness = 180f),
                                label = "weeklyBarHeight"
                            )
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .height(animatedHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (height >= 0.9f) Red else Green)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(day, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = s.reviewsCount.format(reviewCount),
                color = Green,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))

        if (reviewsList.isEmpty()) {
            Text(
                text = "No completed delivery has received a customer rating yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            reviewsList.forEach { review ->
                ReviewCard(review)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RatingBarItem(stars: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 220f),
        label = "ratingDistribution"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(stars, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.width(24.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFFB300))
            )
        }
    }
}

@Composable
private fun ReviewCard(review: CustomerReview) {
    val cardColor = if (LocalDarkMode.current) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFF1F4F8)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
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
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(review.customerName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Order ${review.id} · ${review.timeAgo}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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
                    Text("%.1f".format(review.rating), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = review.comment,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}