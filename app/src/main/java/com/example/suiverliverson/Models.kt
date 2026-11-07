package com.example.suiverliverson

enum class AvatarType {
    MAN, WOMAN, OLD_MAN, YOUNG
}

data class Order(
    val id: String,
    val customerName: String,
    val distanceKm: Double,
    val price: Double,
    val pickup: String,
    val client: String,
    val orderType: String = "Food / طعام",
    val avatarType: AvatarType = AvatarType.MAN
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
    val pickups = listOf("Pizza Hut المعاريف", "Tacos de Lyon أنفا", "Burger King غوتييه", "KFC توين سنتر", "McDo عين الذئاب")
    val clients = listOf("شارع أنفا، الدار البيضاء", "حي المعاريف", "شارع غوتييه", "حي بورغون", "عين الذئاب", "حي النخيل")

    return (1..3).map {
        val (name, avatarType) = customers.random()
        Order(
            id = "#" + (10000..99999).random(),
            customerName = name,
            distanceKm = kotlin.math.round((1.0 + (6.0 - 1.0) * Math.random()) * 10) / 10,
            price = kotlin.math.round((5.0 + (25.0 - 5.0) * Math.random()) * 100) / 100,
            pickup = pickups.random(),
            client = clients.random(),
            orderType = orderTypes.random(),
            avatarType = avatarType
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
    val pickups = listOf("Pizza Hut المعاريف", "Tacos de Lyon أنفا", "Burger King غوتييه", "KFC توين سنتر", "McDo عين الذئاب")
    val clients = listOf("شارع أنفا، الدار البيضاء", "حي المعاريف", "شارع غوتييه", "حي بورغون", "عين الذئاب", "حي النخيل")

    val (name, avatarType) = customers.random()
    return Order(
        id = "#HTO" + (10000..99999).random(),
        customerName = name,
        distanceKm = kotlin.math.round((1.2 + (7.5 - 1.2) * Math.random()) * 10) / 10,
        price = kotlin.math.round((6.5 + (30.0 - 6.5) * Math.random()) * 100) / 100,
        pickup = pickups.random(),
        client = clients.random(),
        orderType = orderTypes.random(),
        avatarType = avatarType
    )
}

fun generateRandomReview(): CustomerReview {
    val comments = listOf(
        "سائق ممتاز وسريع جداً في التوصيل! شكراً لك 👌",
        "Super fast delivery, food was still warm and fresh! 🌟",
        "Service très rapide et livreur très poli. Merci beaucoup! 👍",
        "توصيل ممتاز وفي الوقت المحدد بالضبط 🚗💨",
        "خلوق وسريع، أحييك على التعامل الرائع! ❤️",
        "Très satisfait de la livraison, parfait!"
    )
    val names = listOf(
        Pair("Yassine Khelifi", AvatarType.MAN),
        Pair("Sonia Mehdi", AvatarType.WOMAN),
        Pair("El-Hadj Mohamed", AvatarType.OLD_MAN),
        Pair("Amine Touati", AvatarType.MAN),
        Pair("Meriem Benali", AvatarType.WOMAN),
        Pair("Walid Ziani", AvatarType.YOUNG)
    )

    val (name, avatarType) = names.random()
    val ratings = listOf(5.0f, 5.0f, 4.5f, 5.0f, 4.0f)

    return CustomerReview(
        id = "#REV" + (1000..9999).random(),
        customerName = name,
        rating = ratings.random(),
        comment = comments.random(),
        timeAgo = "الآن",
        avatarType = avatarType
    )
}

val initialReviewsList = listOf(
    CustomerReview("#REV101", "Kamel Ouali", 5.0f, "توصيل في القمة وسريع جداً!", "قبل 15 دقيقة", AvatarType.MAN),
    CustomerReview("#REV102", "Ania Hadj", 4.5f, "Service parfait et livreur très sympa.", "قبل ساعة", AvatarType.WOMAN),
    CustomerReview("#REV103", "Ammi Ahmed", 5.0f, "ماشاء الله تعامل راقي وسريع.", "قبل ساعتين", AvatarType.OLD_MAN)
)