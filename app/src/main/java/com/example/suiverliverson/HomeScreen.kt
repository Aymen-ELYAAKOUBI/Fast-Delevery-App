package com.example.suiverliverson

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun HomeScreen(
    deliveryManager: DeliveryManager,
    onNavigateToMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onOrderClick: (Order) -> Unit = {}
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val account = remember { AccountStore(context).current() }
    val imageUriString = account?.imageUri
    var online by remember { mutableStateOf(true) }

    var availableOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var orderLoadError by remember { mutableStateOf<String?>(null) }
    var incomingOrder by remember { mutableStateOf<Order?>(null) }
    val orderRepository = remember { OrderRepository() }
    val driverUid = FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(online, driverUid) {
        var registration: ListenerRegistration? = null
        if (online && driverUid != null) {
            registration = orderRepository.listenForAvailable({ requests ->
                availableOrders = requests.map { it.asDriverOrder() }
                orderLoadError = null
            }, { message -> orderLoadError = message })
        } else {
            availableOrders = emptyList()
        }
        onDispose { registration?.remove() }
    }

    val userBitmap = remember(imageUriString) {
        if (!imageUriString.isNullOrEmpty()) {
            try {
                context.contentResolver.openInputStream(Uri.parse(imageUriString)).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // نافذة إشعار الطلب الجديد
    incomingOrder?.let { order ->
        Dialog(onDismissRequest = { incomingOrder = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Red)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = s.incomingOrder,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomerAvatar(type = order.avatarType)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.customerName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(order.id, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Green)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("$${"%.2f".format(order.price)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = s.orderCategory.format(order.orderType),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Red))
                            Spacer(Modifier.width(8.dp))
                            Text("من (Pickup): ${order.pickup}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Green))
                            Spacer(Modifier.width(8.dp))
                            Text("إلى (Delivery): ${order.client}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("المسافة: ${order.distanceKm} كم", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { incomingOrder = null },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(s.reject, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                deliveryManager.acceptOrder(order)
                                availableOrders = availableOrders.filterNot { it.id == order.id }
                                incomingOrder = null
                                onNavigateToMap()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(s.accept, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // الشريط العلوي بأيقونة NotificationsActive الحمراء الأنيقة
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    // The live Firestore listener refreshes this list automatically.
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(4.dp))
                // أيقونة NotificationsActive الحمراء المميزة لفتح صفحة الإشعارات
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Red,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // كارد حالة الاتصال
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    if (userBitmap != null) {
                        Image(
                            bitmap = userBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                if (account?.gender == "woman") R.drawable.woman_avatar else R.drawable.man_avatar
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (online) Green else MaterialTheme.colorScheme.outline)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (online) s.youAreOnline else s.youAreOffline,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (online) s.readyForOrders else s.goOnlineToStart,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = online,
                    onCheckedChange = { online = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Green,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // الإحصائيات الأربعة للمغرب (الدار البيضاء)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Default.AttachMoney,
                tint = Green,
                value = "$${"%.2f".format(deliveryManager.totalEarnings)}",
                label = s.today,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.CheckCircle,
                tint = Color(0xFF42A5F5),
                value = "${deliveryManager.completedTrips}",
                label = s.tripsCompleted,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Default.Star,
                tint = Color(0xFFFFB300),
                value = "%.1f".format(deliveryManager.rating),
                label = s.rating,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.LocationOn,
                tint = Red,
                value = account?.location?.ifBlank { "Casablanca" } ?: "Casablanca",
                label = s.currentLocation,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // كارد الرحلة النشطة
        val activeOrder = deliveryManager.currentActiveOrder
        if (activeOrder != null) {
            Text(
                text = s.activeTrip,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMap() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.orderNumber.format(activeOrder.id), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(s.customer.format(activeOrder.customerName), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Red)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(s.inTransit, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Red))
                        Box(modifier = Modifier.weight(0.6f).height(4.dp).background(Red))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${activeOrder.distanceKm} km", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.weight(0.4f).height(4.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(activeOrder.pickup, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(activeOrder.client, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Green)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // طلبات الرحلات الجديدة
        if (online) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.newTripRequests,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    // The live Firestore listener refreshes this list automatically.
                }) {
                    Text(s.seeAll, color = Red, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            orderLoadError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            if (availableOrders.isEmpty() && orderLoadError == null) {
                Text("No delivery requests nearby right now.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }

            availableOrders.forEach { order ->
                NewTripRequestCard(
                    order = order,
                    onClick = { incomingOrder = order },
                    infoText = s.info
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CustomerAvatar(type: AvatarType) {
    val (bgColor, emoji) = when (type) {
        AvatarType.WOMAN -> Pair(Color(0xFFE91E63), "👩‍🦰")
        AvatarType.MAN -> Pair(Color(0xFF1976D2), "👨")
        AvatarType.OLD_MAN -> Pair(Color(0xFF8D6E63), "👴")
        AvatarType.YOUNG -> Pair(Color(0xFF4CAF50), "👦")
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 22.sp)
    }
}

@Composable
private fun NewTripRequestCard(order: Order, onClick: () -> Unit, infoText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerAvatar(type = order.avatarType)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(order.customerName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "${order.id} • $${"%.2f".format(order.price)} • ${order.distanceKm} km",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    "من: ${order.pickup}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(infoText, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}