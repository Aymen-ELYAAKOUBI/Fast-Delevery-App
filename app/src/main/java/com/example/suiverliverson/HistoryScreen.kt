package com.example.suiverliverson

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun HistoryScreen(deliveryManager: DeliveryManager, onBack: () -> Unit) {
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // الشريط العلوي مع زر العودة ⬅️
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = s.orderHistory,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // ملخص عدد الطلبات المكتملة في السجل
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Red)
                    Spacer(Modifier.width(10.dp))
                    Text("إجمالي الطلبات المكتملة", color = LightGray, fontSize = 14.sp)
                }
                Text("${deliveryManager.completedOrdersList.size}", color = Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (deliveryManager.completedOrdersList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(s.noHistory, color = LightGray, fontSize = 15.sp)
            }
        } else {
            deliveryManager.completedOrdersList.forEach { completed ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(completed.customerName, color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Green)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("+$${"%.2f".format(completed.price)}", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${completed.id} • ${completed.completedAt}", color = LightGray, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("من: ${completed.pickup} ➡️ إلى: ${completed.delivery}", color = LightGray.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}