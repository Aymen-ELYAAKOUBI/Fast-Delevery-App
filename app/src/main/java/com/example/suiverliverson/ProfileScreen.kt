package com.example.suiverliverson

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

// QR Code حقيقي قابل للمسح، مولَّد بمكتبة ZXing ومرسوم بنفس شكل الصورة
@Composable
fun ProfessionalQrCode(data: String, modifier: Modifier = Modifier) {
    val matrix = remember(data) {
        QRCodeWriter().encode(
            data,
            BarcodeFormat.QR_CODE,
            0,
            0,
            mapOf<EncodeHintType, Any>(
                EncodeHintType.MARGIN to 0,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val n = matrix.width
                val cell = size.width / n
                for (r in 0 until n) {
                    for (c in 0 until n) {
                        if (matrix.get(c, r)) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(c * cell, r * cell),
                                size = Size(cell + 0.6f, cell + 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    store: AccountStore,
    deliveryManager: DeliveryManager,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val account = store.current()

    var showQrDialog by remember { mutableStateOf(false) }

    val userBitmap = remember(account?.imageUri) {
        if (!account?.imageUri.isNullOrEmpty()) {
            try {
                context.contentResolver.openInputStream(Uri.parse(account!!.imageUri)).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.profile,
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    if (userBitmap != null) {
                        Image(
                            bitmap = userBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, Red, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, Red, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${account?.firstName ?: "Driver"} ${account?.lastName ?: ""}",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${account?.id ?: "#DRV1024"}",
                    color = Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showQrDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text(s.qrCode, color = White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // كارد الانتقال لصفحة السجل الكاملة
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToHistory() },
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
                    Text(s.orderHistory, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text("${deliveryManager.completedOrdersList.size} >", color = Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = s.driverInfo,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(Icons.Default.Phone, "Téléphone / رقم الهاتف", account?.login ?: "+212 6 12 34 56 78")
                ProfileInfoRow(Icons.Default.Email, "E-mail / البريد الإلكتروني", "${account?.firstName?.lowercase() ?: "driver"}@liverson.ma")
                ProfileInfoRow(Icons.Default.Badge, s.licenseType, "Permis: ${account?.license ?: "B"}")
                ProfileInfoRow(Icons.Default.Person, s.age, "${account?.age ?: 25} ans")
                ProfileInfoRow(Icons.AutoMirrored.Filled.DirectionsBike, "Véhicule / الدراجة", "Scooter 125cc (الدار البيضاء 🇲🇦)")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Red)
            Spacer(Modifier.width(8.dp))
            Text(s.logout, color = Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = s.qrCode,
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    ProfessionalQrCode(
                        data = "LIVERSON_DRIVER_${account?.id ?: "DRV1024"}",
                        modifier = Modifier.size(240.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("ID: ${account?.id ?: "#DRV1024"}", color = Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { showQrDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Fermer / إغلاق", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Red, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = LightGray, fontSize = 12.sp)
            Text(value, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}