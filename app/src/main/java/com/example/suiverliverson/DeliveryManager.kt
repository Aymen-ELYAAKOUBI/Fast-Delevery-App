package com.example.suiverliverson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class DeliveryStage {
    IDLE,
    PICKUP_EN_ROUTE,
    ORDER_PICKED_UP,
    DELIVERED
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String = "الآن"
)

data class CompletedOrder(
    val id: String,
    val customerName: String,
    val price: Double,
    val distanceKm: Double,
    val pickup: String,
    val delivery: String,
    val completedAt: String = "اليوم"
)

class DeliveryManager {
    var totalEarnings by mutableDoubleStateOf(186.00)
    var completedTrips by mutableIntStateOf(112)
    var rating by mutableDoubleStateOf(0.0)

    var currentActiveOrder by mutableStateOf<Order?>(null)
    var deliveryStage by mutableStateOf(DeliveryStage.IDLE)

    var activeRouteStartPoint by mutableStateOf<GeoPoint?>(null)
    var activeRoadRoute by mutableStateOf<List<GeoPoint>>(emptyList())
    var routeProgress by mutableFloatStateOf(0f)
    var pickupRouteProgress by mutableFloatStateOf(0.5f)

    val notificationsList = mutableStateListOf(
        AppNotification("#N1", "Commande livrée avec succès !", "الطلب #HTO80120 مع العميلة Yasmine Bennani"),
        AppNotification("#N2", "Commande récupérée !", "تم استلام الطلب من Pizza Hut المعاريف"),
        AppNotification("#N3", "Nouveau commande reçu !", "طلب جديد م مبلغه $24.50")
    )

    val customerReviewsList = mutableStateListOf<CustomerReview>()

    val completedOrdersList = mutableStateListOf(
        CompletedOrder("#HTO80120", "Yasmine Bennani", 24.50, 3.2, "Pizza Hut المعاريف", "شارع أنفا، الدار البيضاء", "اليوم 14:30"),
        CompletedOrder("#HTO78110", "Sofiane Touati", 18.00, 2.5, "Tacos de Lyon أنفا", "حي المعاريف", "اليوم 12:15"),
        CompletedOrder("#HTO65432", "Nour-El-Houda", 32.00, 5.0, "Burger King غوتييه", "حي بورغون", "أمس 19:40")
    )

    fun acceptOrder(order: Order) {
        if (currentActiveOrder != null && deliveryStage != DeliveryStage.DELIVERED) return

        currentActiveOrder = order
        activeRouteStartPoint = casablancaDriverStartPoints.random()
        activeRoadRoute = emptyList()
        routeProgress = 0f
        pickupRouteProgress = 0.5f
        deliveryStage = DeliveryStage.PICKUP_EN_ROUTE
        notificationsList.add(0, AppNotification("#N${(100..999).random()}", "Nouveau commande accepté !", "Commande ${order.id} pour ${order.customerName}"))
    }

    fun completePickup() {
        if (deliveryStage != DeliveryStage.PICKUP_EN_ROUTE) return

        deliveryStage = DeliveryStage.ORDER_PICKED_UP
        val order = currentActiveOrder
        notificationsList.add(0, AppNotification("#N${(100..999).random()}", "Commande récupérée !", "الطلب ${order?.id ?: ""} تم استلامه من ${order?.pickup ?: ""}"))
    }

    fun completeDelivery() {
        if (deliveryStage != DeliveryStage.ORDER_PICKED_UP) return

        val order = currentActiveOrder ?: return
        totalEarnings += order.price
        completedTrips += 1
        deliveryStage = DeliveryStage.DELIVERED

        completedOrdersList.add(
            0,
            CompletedOrder(
                id = order.id,
                customerName = order.customerName,
                price = order.price,
                distanceKm = order.distanceKm,
                pickup = order.pickup,
                delivery = order.client,
                completedAt = "الآن"
            )
        )

        if (customerReviewsList.none { it.id == order.id }) {
            customerReviewsList.add(0, generateReviewForOrder(order))
            rating = customerReviewsList.map { it.rating }.average()
        }

        notificationsList.add(0, AppNotification("#N${(100..999).random()}", "Commande livrée avec succès !", "Commande ${order.id} livrée à ${order.client}"))

        currentActiveOrder = null
        activeRouteStartPoint = null
        activeRoadRoute = emptyList()
        routeProgress = 0f
        pickupRouteProgress = 0.5f
        deliveryStage = DeliveryStage.IDLE
    }
}