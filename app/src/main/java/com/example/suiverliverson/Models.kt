package com.example.suiverliverson

enum class AvatarType {
    MAN, WOMAN, OLD_MAN, YOUNG
}

data class GeoPoint(val latitude: Double, val longitude: Double)

data class DeliveryPlace(val name: String, val point: GeoPoint)

val casablancaPickupPlaces = listOf(
    DeliveryPlace("Pizza Hut المعاريف", GeoPoint(33.5834, -7.6322)),
    DeliveryPlace("Tacos de Lyon أنفا", GeoPoint(33.5890, -7.6270)),
    DeliveryPlace("Burger King غوتييه", GeoPoint(33.5910, -7.6210)),
    DeliveryPlace("KFC توين سنتر", GeoPoint(33.5879, -7.6333)),
    DeliveryPlace("McDo عين الذئاب", GeoPoint(33.5960, -7.6720))
)

val casablancaDeliveryPlaces = listOf(
    DeliveryPlace("شارع أنفا، الدار البيضاء", GeoPoint(33.5906, -7.6340)),
    DeliveryPlace("حي المعاريف", GeoPoint(33.5830, -7.6350)),
    DeliveryPlace("شارع غوتييه", GeoPoint(33.5920, -7.6210)),
    DeliveryPlace("حي بورغون", GeoPoint(33.5970, -7.6510)),
    DeliveryPlace("عين الذئاب", GeoPoint(33.5940, -7.6690)),
    DeliveryPlace("حي النخيل", GeoPoint(33.5790, -7.6250))
)

val casablancaDriverStartPoints = listOf(
    GeoPoint(33.5724, -7.6170),
    GeoPoint(33.5780, -7.6460),
    GeoPoint(33.6000, -7.6380),
    GeoPoint(33.5680, -7.6320),
    GeoPoint(33.5860, -7.6060)
)

data class Order(
    val id: String,
    val customerName: String,
    val distanceKm: Double,
    val price: Double,
    val pickup: String,
    val client: String,
    val orderType: String = "Food / طعام",
    val avatarType: AvatarType = AvatarType.MAN,
    val pickupLocation: GeoPoint? = null,
    val deliveryLocation: GeoPoint? = null
)

data class ActiveTripData(
    val orderId: String,
    val customerName: String,
    val distanceKm: Double,
    val pickup: String,
    val delivery: String,
    val statusText: String
)

data class CustomerReview(
    val id: String,
    val customerName: String,
    val rating: Float,
    val comment: String,
    val timeAgo: String,
    val avatarType: AvatarType
)

var defaultActiveTrip = ActiveTripData(
    orderId = "#HTO28704",
    customerName = "Karim Touati",
    distanceKm = 5.2,
    pickup = "Pizza Hut المعاريف",
    delivery = "شارع أنفا، الدار البيضاء",
    statusText = "In Transit"
)

fun generateRandomOrdersList(): List<Order> {
    val customers = listOf(
        Pair("Sarah Larbi", AvatarType.WOMAN),
        Pair("Karim Touati", AvatarType.MAN),
        Pair("El-Hadj Mohamed", AvatarType.OLD_MAN),
        Pair("Yassine Mansouri", AvatarType.MAN),
        Pair("Lina Benali", AvatarType.WOMAN),
        Pair("Ammi Rachid", AvatarType.OLD_MAN),
        Pair("Nadia Khelil", AvatarType.WOMAN)
    )
    val orderTypes = listOf("Food / طعام", "Fast Food / وجبات سريعة", "Groceries / بقالة", "Pharmacy / دواء")

    return (1..3).map {
        val (name, avatarType) = customers.random()
        val pickupPlace = casablancaPickupPlaces.random()
        val deliveryPlace = casablancaDeliveryPlaces.random()
        Order(
            id = "#" + (10000..99999).random(),
            customerName = name,
            distanceKm = kotlin.math.round((1.0 + (6.0 - 1.0) * Math.random()) * 10) / 10,
            price = kotlin.math.round((5.0 + (25.0 - 5.0) * Math.random()) * 100) / 100,
            pickup = pickupPlace.name,
            client = deliveryPlace.name,
            orderType = orderTypes.random(),
            avatarType = avatarType,
            pickupLocation = pickupPlace.point,
            deliveryLocation = deliveryPlace.point
        )
    }
}

fun generateRandomIncomingOrder(): Order {
    val customers = listOf(
        Pair("Yasmine Bennani", AvatarType.WOMAN),
        Pair("Sofiane Touati", AvatarType.MAN),
        Pair("El-Hadj Hassan", AvatarType.OLD_MAN),
        Pair("Nour-El-Houda", AvatarType.WOMAN),
        Pair("Bilal Khelil", AvatarType.YOUNG)
    )
    val orderTypes = listOf("Food / طعام 🍔", "Fast Food / وجبات سريعة 🍕", "Groceries / بقالة 🛒", "Pharmacy / دواء 💊")

    val (name, avatarType) = customers.random()
    val pickupPlace = casablancaPickupPlaces.random()
    val deliveryPlace = casablancaDeliveryPlaces.random()
    return Order(
        id = "#HTO" + (10000..99999).random(),
        customerName = name,
        distanceKm = kotlin.math.round((1.2 + (7.5 - 1.2) * Math.random()) * 10) / 10,
        price = kotlin.math.round((6.5 + (30.0 - 6.5) * Math.random()) * 100) / 100,
        pickup = pickupPlace.name,
        client = deliveryPlace.name,
        orderType = orderTypes.random(),
        avatarType = avatarType,
        pickupLocation = pickupPlace.point,
        deliveryLocation = deliveryPlace.point
    )
}

fun generateReviewForOrder(order: Order): CustomerReview {
    val rating = (1..5).random()
    val comments = when (rating) {
        5 -> listOf("Excellent delivery, thank you!", "Fast and friendly service!", "توصيل ممتاز، شكراً لك!")
        4 -> listOf("Good service and delivered on time.", "Très bon service, merci.")
        3 -> listOf("The delivery was okay.", "الخدمة مقبولة.")
        2 -> listOf("The delivery was late.", "التوصيل كان متأخراً.")
        else -> listOf("Poor delivery experience.", "تجربة توصيل سيئة.")
    }

    return CustomerReview(
        id = order.id,
        customerName = order.customerName,
        rating = rating.toFloat(),
        comment = comments.random(),
        timeAgo = "الآن",
        avatarType = order.avatarType
    )
}