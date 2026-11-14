package com.example.suiverliverson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.example.suiverliverson.ui.theme.Red

@Composable
fun RoleHomeScreen(account: Account, onLogout: () -> Unit) {
    val isCompany = account.role == AccountRole.COMPANY
    val displayName = if (isCompany && account.companyName.isNotBlank()) account.companyName
        else "${account.firstName} ${account.lastName}".trim()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val repository = remember { OrderRepository() }
    var orders by remember { mutableStateOf<List<DeliveryRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickup by remember { mutableStateOf("") }
    var delivery by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    DisposableEffect(uid) {
        var registration: ListenerRegistration? = null
        if (uid != null) {
            registration = repository.listenForOwner(uid, { result ->
                orders = result
                loading = false
                error = null
            }, { message ->
                loading = false
                error = message
            })
        } else {
            loading = false
            error = "Sign in again to access your delivery workspace."
        }
        onDispose { registration?.remove() }
    }

    val activeCount = orders.count { it.status in listOf("requested", "accepted", "picked_up") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(if (isCompany) "Company workspace" else "Client deliveries", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hello, $displayName", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, contentDescription = "Log out", tint = Red) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard("Total requests", orders.size.toString(), Modifier.weight(1f))
            SummaryCard("In progress", activeCount.toString(), Modifier.weight(1f))
            if (isCompany) SummaryCard("Delivered", orders.count { it.status == "delivered" }.toString(), Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Red)
                    Text(if (isCompany) "Create a company delivery" else "Request a delivery", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Enter the pickup and destination details. Your request will appear in the live driver queue.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                OutlinedTextField(pickup, { pickup = it }, Modifier.fillMaxWidth(), label = { Text("Pickup address") }, singleLine = true)
                OutlinedTextField(delivery, { delivery = it }, Modifier.fillMaxWidth(), label = { Text("Delivery address") }, singleLine = true)
                OutlinedTextField(recipient, { recipient = it }, Modifier.fillMaxWidth(), label = { Text("Recipient name") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Recipient phone") }, singleLine = true)
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Package or order details") }, minLines = 2)
                Button(
                    onClick = {
                        submitting = true
                        notice = null
                        repository.create(account, pickup, delivery, recipient, phone, description) { result ->
                            submitting = false
                            if (result == null) {
                                pickup = ""; delivery = ""; recipient = ""; phone = ""; description = ""
                                notice = "Delivery request created."
                            } else notice = result
                        }
                    },
                    enabled = !submitting && pickup.isNotBlank() && delivery.isNotBlank() && recipient.isNotBlank() && phone.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (submitting) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                    else Text(if (isCompany) "Add delivery request" else "Request delivery")
                }
                notice?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (isCompany) "Company delivery management" else "Your deliveries", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Refresh, contentDescription = "Live updates", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (isCompany) "Requests submitted by this company account, with live driver and delivery status." else "Track each request from submission through delivery.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (loading) CircularProgressIndicator()
        else if (orders.isEmpty() && error == null) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp)) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Red)
                    Spacer(Modifier.height(8.dp))
                    Text("No deliveries yet", fontWeight = FontWeight.SemiBold)
                    Text("Create a delivery request above to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            orders.forEach { request ->
                DeliveryRequestCard(request)
            }
        }

        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Log out") }
    }
}

@Composable
fun ApprovalPendingScreen(account: Account, onLogout: () -> Unit) {
    val displayName = if (account.role == AccountRole.COMPANY && account.companyName.isNotBlank()) {
        account.companyName
    } else {
        "${account.firstName} ${account.lastName}".trim()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Account review", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = "Log out", tint = Red)
            }
        }
        Text("Hello, $displayName", fontSize = 18.sp)
        Text(
            "Your ${account.role.label.lowercase()} account is waiting for approval. " +
                "You can use delivery features after the account is approved.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "After approval, log out and sign in again to refresh your account status.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DeliveryRequestCard(request: DeliveryRequest) {
    val statusLabel = when (request.status) {
        "requested" -> "Waiting for a driver"
        "accepted" -> "Driver assigned"
        "picked_up" -> "Picked up"
        "delivered" -> "Delivered"
        "cancelled" -> "Cancelled"
        else -> request.status.replaceFirstChar { it.uppercase() }
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Request #${request.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                Text(statusLabel, color = if (request.status == "delivered") MaterialTheme.colorScheme.primary else Red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("From: ${request.pickup}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text("To: ${request.delivery}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text("Recipient: ${request.recipientName} · ${request.recipientPhone}", fontSize = 12.sp)
            if (request.packageDescription.isNotBlank()) Text(request.packageDescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (request.driverUid.isNullOrBlank()) "No driver assigned yet" else "A driver has accepted this request", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
