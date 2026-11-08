package com.example.suiverliverson

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.Red

@Composable
fun SettingsScreen(store: AccountStore, onBack: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val account = store.current()
    val currentLang = LocalLang.current
    val setLang = LocalSetLang.current
    val darkMode = LocalDarkMode.current
    val setDarkMode = LocalSetDarkMode.current
    val orderNotificationsEnabled = LocalOrderNotificationsEnabled.current
    val setOrderNotificationsEnabled = LocalSetOrderNotificationsEnabled.current

    var editFirstName by remember { mutableStateOf(account?.firstName ?: "") }
    var editLastName by remember { mutableStateOf(account?.lastName ?: "") }
    var editPhone by remember { mutableStateOf(account?.login ?: "") }
    var editLocation by remember { mutableStateOf(account?.location ?: "") }
    var editImageUri by remember { mutableStateOf<Uri?>(account?.imageUri?.let { Uri.parse(it) }) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { granted ->
        setOrderNotificationsEnabled(granted)
        if (!granted) {
            val activity = context as? Activity
            if (activity != null && !activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showNotificationSettings = true
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) editImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = s.settings,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // الصورة الشخصية
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { photoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (editImageUri != null) {
                        val bitmap = remember(editImageUri) {
                            try {
                                context.contentResolver.openInputStream(editImageUri!!).use {
                                    BitmapFactory.decodeStream(it)
                                }
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(
                                if (account?.gender == "woman") R.drawable.woman_avatar else R.drawable.man_avatar
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = editFirstName,
                    onValueChange = { editFirstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.firstName, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editLastName,
                    onValueChange = { editLastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.lastName, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Téléphone / Phone", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editLocation,
                    onValueChange = { editLocation = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location / المدينة", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                // اللغة
                Text("Langue / اللغة", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "EN", "fr" to "FR", "ar" to "عربي").forEach { (code, name) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentLang == code) Red else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { setLang(code) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(name, color = if (currentLang == code) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark mode / الوضع الداكن", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    Switch(
                        checked = darkMode,
                        onCheckedChange = setDarkMode,
                        colors = SwitchDefaults.colors(checkedTrackColor = Green)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.enableNotifications, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    Switch(
                        checked = orderNotificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                setOrderNotificationsEnabled(false)
                            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                setOrderNotificationsEnabled(true)
                            } else if (
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                setOrderNotificationsEnabled(true)
                            } else {
                                val activity = context as? Activity
                                val rationaleNeeded = activity?.shouldShowRequestPermissionRationale(
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == true
                                val permissionWasRequested = context.getSharedPreferences("settings", 0)
                                    .getBoolean("notification_permission_asked", false)
                                when {
                                    rationaleNeeded -> showNotificationRationale = true
                                    permissionWasRequested -> showNotificationSettings = true
                                    else -> {
                                        context.getSharedPreferences("settings", 0).edit()
                                            .putBoolean("notification_permission_asked", true)
                                            .apply()
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Green)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (account != null) {
                            val updated = account.copy(
                                firstName = editFirstName,
                                lastName = editLastName,
                                login = editPhone,
                                location = editLocation.trim(),
                                imageUri = editImageUri?.toString()
                            )
                            store.updateAccount(updated)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(s.save, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationRationale = false },
            title = { Text("Allow order notifications?") },
            text = { Text("Fast Delivery needs notification permission to alert you when a new delivery order arrives.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    context.getSharedPreferences("settings", 0).edit()
                        .putBoolean("notification_permission_asked", true)
                        .apply()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationRationale = false }) { Text("Not now") }
            }
        )
    }

    if (showNotificationSettings) {
        AlertDialog(
            onDismissRequest = { showNotificationSettings = false },
            title = { Text("Notifications are disabled") },
            text = { Text("Allow notifications for Fast Delivery in Android app settings to receive new order alerts.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationSettings = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationSettings = false }) { Text("Cancel") }
            }
        )
    }
}