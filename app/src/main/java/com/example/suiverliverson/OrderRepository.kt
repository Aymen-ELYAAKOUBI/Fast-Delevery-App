package com.example.suiverliverson

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue

data class DeliveryRequest(
    val id: String,
    val ownerUid: String,
    val ownerRole: String,
    val customerName: String,
    val pickup: String,
    val delivery: String,
    val recipientName: String,
    val recipientPhone: String,
    val packageDescription: String,
    val status: String,
    val driverUid: String?,
    val createdAt: Long,
    val price: Double = 20.0,
    val distanceKm: Double = 0.0
) {
    fun asDriverOrder() = Order(
        id = id,
        customerName = customerName,
        distanceKm = distanceKm,
        price = price,
        pickup = pickup,
        client = delivery,
        orderType = packageDescription.ifBlank { "Delivery / توصيل" }
    )

    companion object {
        fun from(document: com.google.firebase.firestore.DocumentSnapshot): DeliveryRequest? {
            val ownerUid = document.getString("ownerUid") ?: return null
            return DeliveryRequest(
                id = document.id,
                ownerUid = ownerUid,
                ownerRole = document.getString("ownerRole") ?: "client",
                customerName = document.getString("customerName") ?: "Customer",
                pickup = document.getString("pickup") ?: "",
                delivery = document.getString("delivery") ?: "",
                recipientName = document.getString("recipientName") ?: "",
                recipientPhone = document.getString("recipientPhone") ?: "",
                packageDescription = document.getString("packageDescription") ?: "",
                status = document.getString("status") ?: "requested",
                driverUid = document.getString("driverUid"),
                createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                price = document.getDouble("price") ?: 20.0,
                distanceKm = document.getDouble("distanceKm") ?: 0.0
            )
        }
    }
}

class OrderRepository {
    private val db = FirebaseFirestore.getInstance(FirebaseFirestoreConfig.DATABASE_ID)
    private val auth get() = FirebaseAuth.getInstance()

    fun listenForOwner(uid: String, onOrders: (List<DeliveryRequest>) -> Unit, onError: (String) -> Unit): ListenerRegistration =
        db.collection("orders").whereEqualTo("ownerUid", uid).addSnapshotListener { snapshot, error ->
            if (error != null) { onError(error.localizedMessage ?: "Could not load orders"); return@addSnapshotListener }
            onOrders(snapshot?.documents.orEmpty().mapNotNull(DeliveryRequest::from).sortedByDescending { it.createdAt })
        }

    fun listenForAvailable(onOrders: (List<DeliveryRequest>) -> Unit, onError: (String) -> Unit): ListenerRegistration =
        db.collection("orders").whereEqualTo("status", "requested").addSnapshotListener { snapshot, error ->
            if (error != null) { onError(error.localizedMessage ?: "Could not load delivery requests"); return@addSnapshotListener }
            onOrders(snapshot?.documents.orEmpty().mapNotNull(DeliveryRequest::from).sortedByDescending { it.createdAt })
        }

    fun listenForDriver(uid: String, onOrders: (List<DeliveryRequest>) -> Unit, onError: (String) -> Unit): ListenerRegistration =
        db.collection("orders").whereEqualTo("driverUid", uid).addSnapshotListener { snapshot, error ->
            if (error != null) { onError(error.localizedMessage ?: "Could not load assigned delivery"); return@addSnapshotListener }
            onOrders(snapshot?.documents.orEmpty().mapNotNull(DeliveryRequest::from))
        }

    fun create(account: Account, pickup: String, delivery: String, recipient: String, phone: String, description: String, onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onResult("Please sign in again"); return }
        val name = if (account.role == AccountRole.COMPANY) account.companyName else "${account.firstName} ${account.lastName}".trim()
        val reference = db.collection("orders").document()
        val data = mapOf(
            "ownerUid" to uid,
            "ownerRole" to account.role.value,
            "customerName" to name,
            "pickup" to pickup.trim(),
            "delivery" to delivery.trim(),
            "recipientName" to recipient.trim(),
            "recipientPhone" to phone.trim(),
            "packageDescription" to description.trim(),
            "status" to "requested",
            "driverUid" to "",
            "price" to 20.0,
            "distanceKm" to 0.0,
            "createdAt" to FieldValue.serverTimestamp()
        )
        reference.set(data).addOnSuccessListener { onResult(null) }
            .addOnFailureListener { onResult(it.localizedMessage ?: "Could not create delivery request") }
    }

    fun claim(requestId: String, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onResult(false, "Please sign in again"); return }
        val ref = db.collection("orders").document(requestId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            check(snapshot.getString("status") == "requested") { "This request was already accepted" }
            transaction.update(ref, mapOf("status" to "accepted", "driverUid" to uid, "updatedAt" to FieldValue.serverTimestamp()))
            true
        }.addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage ?: "Could not accept request") }
    }

    fun updateStatus(requestId: String, status: String, onResult: (String?) -> Unit = {}) {
        db.collection("orders").document(requestId)
            .update(mapOf("status" to status, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(null) }
            .addOnFailureListener { onResult(it.localizedMessage ?: "Could not update delivery status") }
    }
}
