package com.example.suiverliverson

import android.content.Context
import android.location.Geocoder
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.suiverliverson.ui.theme.Blue
import com.example.suiverliverson.ui.theme.Green
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
fun MapScreen(
    deliveryManager: DeliveryManager,
    onDeliveryComplete: () -> Unit
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val activeOrder = deliveryManager.currentActiveOrder
    val darkMode = LocalDarkMode.current
    val account = remember { AccountStore(context).current() }
    var accountLocationPoint by remember(account?.location) { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(account?.location) {
        val location = account?.location?.trim().orEmpty()
        if (location.isNotBlank()) {
            val resolvedPoint = withContext(Dispatchers.IO) {
                try {
                    val query = if (location.contains(",")) location else "$location, Morocco"
                    @Suppress("DEPRECATION")
                    Geocoder(context).getFromLocationName(query, 1)
                        ?.firstOrNull()
                        ?.let { GeoPoint(it.latitude, it.longitude) }
                } catch (_: Exception) {
                    null
                }
            }
            accountLocationPoint = resolvedPoint
            if (resolvedPoint != null && activeOrder != null) {
                deliveryManager.activeRouteStartPoint = resolvedPoint
                deliveryManager.activeRoadRoute = emptyList()
            }
        }
    }

    val activeOrderId = activeOrder?.id
    val driverStartPoint = accountLocationPoint
        ?: deliveryManager.activeRouteStartPoint
        ?: casablancaDriverStartPoints.first()
    val pickupPoint = activeOrder?.pickupLocation ?: casablancaPickupPlaces.first().point
    val deliveryPoint = activeOrder?.deliveryLocation ?: casablancaDeliveryPlaces.first().point
    val roadRoute = deliveryManager.activeRoadRoute
    val progress = deliveryManager.routeProgress
    val driverStartPos = LatLng(driverStartPoint.latitude, driverStartPoint.longitude)
    val pickupPos = LatLng(pickupPoint.latitude, pickupPoint.longitude)
    val deliveryPos = LatLng(deliveryPoint.latitude, deliveryPoint.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverStartPos, 13.5f)
    }

    LaunchedEffect(accountLocationPoint, activeOrderId) {
        if (accountLocationPoint != null && activeOrder == null) {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(driverStartPos, 13.5f),
                800
            )
        }
    }

    val mapProperties = remember(darkMode) {
        MapProperties(
            isMyLocationEnabled = false,
            mapStyleOptions = if (darkMode) {
                try { MapStyleOptions(darkMapStyleJson) } catch (e: Exception) { null }
            } else null
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
    }

    var routeLoading by remember(activeOrderId) { mutableStateOf(false) }
    var routeUnavailable by remember(activeOrderId) { mutableStateOf(false) }

    val motorcycleIcon = remember(context) { getMotorcycleIcon(context) }
    val pickupMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏪", "#E91E63") }
    val deliveryMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏠", "#28A745") }

    LaunchedEffect(activeOrderId, driverStartPoint) {
        routeUnavailable = false
        if (activeOrder != null) {
            val route = deliveryManager.activeRoadRoute
            if (route.isEmpty()) {
                routeLoading = true
                try {
                    val apiKey = context.packageManager
                        .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                        .metaData
                        ?.getString("com.google.android.geo.API_KEY")
                        .orEmpty()
                    val fetchedRoute = fetchRoadRoute(
                        apiKey,
                        driverStartPoint,
                        pickupPoint,
                        deliveryPoint
                    )
                    check(fetchedRoute.size > 1) { "No road route was returned." }
                    deliveryManager.activeRoadRoute = fetchedRoute
                    deliveryManager.pickupRouteProgress = progressAtPoint(fetchedRoute, pickupPoint)
                } catch (_: Exception) {
                    routeUnavailable = true
                    Toast.makeText(context, "Could not reach a road routing service. Check your internet and try again.", Toast.LENGTH_LONG).show()
                } finally {
                    routeLoading = false
                }
            }

            val stableRoute = deliveryManager.activeRoadRoute
            if (stableRoute.size > 1) {
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                stableRoute.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120),
                    800
                )
            }
        }
    }

    val currentBikePoint = pointAlongRoute(roadRoute, progress) ?: driverStartPoint
    val currentBikePos = LatLng(currentBikePoint.latitude, currentBikePoint.longitude)

    LaunchedEffect(deliveryManager.deliveryStage, roadRoute) {
        if (deliveryManager.deliveryStage != DeliveryStage.IDLE && roadRoute.size > 1) {
            while (deliveryManager.routeProgress < 1f) {
                delay(1500L)
                deliveryManager.routeProgress =
                    (deliveryManager.routeProgress + 0.015f).coerceAtMost(1f)

                if (deliveryManager.routeProgress >= deliveryManager.pickupRouteProgress &&
                    deliveryManager.deliveryStage == DeliveryStage.PICKUP_EN_ROUTE
                ) {
                    deliveryManager.completePickup()
                    Toast.makeText(context, s.orderStarted, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // خريطة داكنة سوداء
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (roadRoute.size > 1) {
                Polyline(
                    points = roadRoute.map { LatLng(it.latitude, it.longitude) },
                    color = Blue,
                    width = 10f
                )
            }

            if (activeOrder != null) {
                Marker(
                    state = MarkerState(position = pickupPos),
                    icon = pickupMarkerIcon,
                    title = activeOrder.pickup
                )
                Marker(
                    state = MarkerState(position = deliveryPos),
                    icon = deliveryMarkerIcon,
                    title = activeOrder.client
                )
                Marker(
                    state = MarkerState(position = currentBikePos),
                    icon = motorcycleIcon,
                    title = "الدراجة النارية 🛵"
                )
            } else if (accountLocationPoint != null) {
                Marker(
                    state = MarkerState(position = driverStartPos),
                    icon = motorcycleIcon,
                    title = "Driver location"
                )
            }
        }

        if (roadRoute.size > 1 && !routeUnavailable) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            ) {
                Text(
                    text = "© OpenStreetMap contributors",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 10.sp
                )
            }
        }

        if (routeLoading || routeUnavailable) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = if (routeLoading) "Loading road route…" else "Road route unavailable. Check internet and try again.",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 13.sp
                )
            }
        }

        // كارد مدمجة تحت تحتوي اسم الزبون والسعر فقط
        activeOrder?.let { order ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        color = MaterialTheme.colorScheme.onSurface,
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // خيار تسليم الطلبية والتأكيد بـ Confirmer
        if (deliveryManager.deliveryStage == DeliveryStage.ORDER_PICKED_UP && progress >= 1f) {
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
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = s.orderDeliveredConfirm,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            deliveryManager.completeDelivery()
                            onDeliveryComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(s.confirmer, color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}