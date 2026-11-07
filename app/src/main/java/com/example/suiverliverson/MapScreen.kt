package com.example.suiverliverson

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.Blue
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.White
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

// تصميم الثيم الداكن المخصص للخريطة
val darkMapStyleJson = """
[
  {"elementType": "geometry", "stylers": [{"color": "#1d1d2b"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#8ec3b9"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#1a1a2b"}]},
  {"featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{"color": "#d59563"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#d59563"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#263c3f"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#2c2c3e"}]},
  {"featureType": "road", "elementType": "geometry.stroke", "stylers": [{"color": "#212130"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#3d3d5c"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0d0d17"}]}
]
""".trimIndent()

// إنشاء أيقونة الدراجة النارية الزرقاء 🛵
fun getMotorcycleIcon(context: Context): BitmapDescriptor {
    return try {
        MapsInitializer.initialize(context)
        val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#007BFF")
            isAntiAlias = true
        }
        canvas.drawCircle(40f, 40f, 36f, paint)
        val textPaint = Paint().apply {
            textSize = 42f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("🛵", 40f, 54f, textPaint)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
    }
}

// إنشاء أيقونات ماركر الاستلام والتوصيل المدمجة والصغيرة
fun getCompactMarkerIcon(context: Context, emoji: String, colorHex: String): BitmapDescriptor {
    return try {
        MapsInitializer.initialize(context)
        val bitmap = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor(colorHex)
            isAntiAlias = true
        }
        canvas.drawCircle(30f, 30f, 26f, paint)
        val textPaint = Paint().apply {
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(emoji, 30f, 40f, textPaint)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}

@Composable
fun MapScreen(deliveryManager: DeliveryManager) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val activeOrder = deliveryManager.currentActiveOrder

    // إحداثيات الدار البيضاء، المغرب 🇲🇦
    val driverStartPos = LatLng(33.5731, -7.5898)
    val pickupPos = LatLng(33.5850, -7.6300)
    val deliveryPos = LatLng(33.5950, -7.6500)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverStartPos, 14f)
    }

    // حماية الشاشة ومنع حدوث SecurityException
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false,
            mapStyleOptions = try { MapStyleOptions(darkMapStyleJson) } catch (e: Exception) { null }
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    }

    var progress by remember { mutableFloatStateOf(0.0f) }
    var isDeliveredShowDialog by remember { mutableStateOf(false) }

    val motorcycleIcon = remember(context) { getMotorcycleIcon(context) }
    val pickupMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏪", "#E91E63") }
    val deliveryMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏠", "#28A745") }

    val currentBikePos = remember(progress) {
        if (progress <= 0.5f) {
            val t = progress / 0.5f
            LatLng(
                driverStartPos.latitude + (pickupPos.latitude - driverStartPos.latitude) * t,
                driverStartPos.longitude + (pickupPos.longitude - driverStartPos.longitude) * t
            )
        } else {
            val t = (progress - 0.5f) / 0.5f
            LatLng(
                pickupPos.latitude + (deliveryPos.latitude - pickupPos.latitude) * t,
                pickupPos.longitude + (deliveryPos.longitude - pickupPos.longitude) * t
            )
        }
    }

    LaunchedEffect(deliveryManager.deliveryStage) {
        if (deliveryManager.deliveryStage != DeliveryStage.IDLE) {
            while (progress < 1.0f) {
                delay(2200L) // حركة هادئة وبطيئة واقعية
                progress += 0.02f

                if (progress >= 0.5f && deliveryManager.deliveryStage == DeliveryStage.PICKUP_EN_ROUTE) {
                    deliveryManager.completePickup()
                    Toast.makeText(context, s.orderStarted, Toast.LENGTH_SHORT).show()
                }

                if (progress >= 1.0f && deliveryManager.deliveryStage == DeliveryStage.ORDER_PICKED_UP) {
                    isDeliveredShowDialog = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // خريطة داكنة سوداء
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // خط مسار أزرق متناسق
            Polyline(
                points = listOf(driverStartPos, pickupPos, deliveryPos),
                color = Blue,
                width = 10f
            )

            // ماركر استلام صغير
            Marker(
                state = MarkerState(position = pickupPos),
                icon = pickupMarkerIcon,
                title = activeOrder?.pickup ?: "الاستلام"
            )

            // ماركر توصيل صغير
            Marker(
                state = MarkerState(position = deliveryPos),
                icon = deliveryMarkerIcon,
                title = activeOrder?.client ?: "التوصيل"
            )

            // أيقونة الدراجة النارية 🛵
            Marker(
                state = MarkerState(position = currentBikePos),
                icon = motorcycleIcon,
                title = "الدراجة النارية 🛵"
            )
        }

        // كارد مدمجة تحت تحتوي اسم الزبون والسعر فقط
        activeOrder?.let { order ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppSurface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$${"%.2f".format(order.price)}",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // خيار تسليم الطلبية والتأكيد بـ Confirmer
        if (isDeliveredShowDialog) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 30.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Green)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = s.orderDeliveredConfirm,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            deliveryManager.completeDelivery()
                            isDeliveredShowDialog = false
                            progress = 0.0f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(s.confirmer, color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}