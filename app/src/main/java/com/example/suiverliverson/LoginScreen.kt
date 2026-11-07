package com.example.suiverliverson

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Purple
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

private val buildingWidths = listOf(
    0.07f, 0.05f, 0.08f, 0.06f, 0.07f, 0.05f, 0.09f, 0.06f,
    0.07f, 0.05f, 0.08f, 0.06f, 0.07f, 0.05f, 0.08f
)
private val buildingHeights = listOf(
    0.40f, 0.70f, 0.55f, 0.90f, 0.50f, 0.80f, 0.60f, 1.00f,
    0.45f, 0.75f, 0.65f, 0.95f, 0.50f, 0.85f, 0.55f
)

@Composable
fun LoginScreen(
    store: AccountStore,
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val s = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val fieldColors = appFieldColors()

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val w = size.width
            val h = size.height

            drawRect(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xFFB03A8C).copy(alpha = 0.30f))
                )
            )

            var x = 0f
            buildingWidths.forEachIndexed { i, bw ->
                val width = w * bw
                val height = h * buildingHeights[i]
                val top = h - height
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Purple.copy(alpha = 0.40f), Purple.copy(alpha = 0.10f)),
                        startY = top,
                        endY = h
                    ),
                    topLeft = Offset(x + 1.5f, top),
                    size = Size(width - 3f, height)
                )
                var wy = top + 10f
                while (wy < h - 8f) {
                    var wx = x + 8f
                    while (wx < x + width - 10f) {
                        drawRect(
                            Color(0xFFFF6FA8).copy(alpha = 0.22f),
                            Offset(wx, wy),
                            Size(3f, 4f)
                        )
                        wx += 12f
                    }
                    wy += 16f
                }
                x += width
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LiversonLogo(iconSize = 100.dp, textSize = 38.sp)
            Spacer(Modifier.height(16.dp))
            Text(s.driverLogin, color = White, fontSize = 20.sp)
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.emailPhone, color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = LightGray) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.password, color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = LightGray) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = LightGray
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Red, fontSize = 13.sp)
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = s.fillAllFields
                    } else {
                        val account = store.authenticate(email, password)
                        if (account == null) {
                            error = s.invalidCredentials
                        } else {
                            store.setCurrent(account)
                            onLogin()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text(s.login, fontSize = 17.sp, color = White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = White)
            }

            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onForgotPassword) {
                Text(s.forgotPassword, color = LightGray, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(Modifier.weight(1f), color = DarkGray)
                Text(
                    s.or,
                    color = LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(Modifier.weight(1f), color = DarkGray)
            }
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Headset, null, tint = White)
                Spacer(Modifier.width(8.dp))
                Text(s.contactSupport, color = White)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.noAccount, color = LightGray, fontSize = 14.sp)
                TextButton(onClick = onCreateAccount) {
                    Text(s.createAccount, color = Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))
            LanguageSelector()
            Spacer(Modifier.height(110.dp))
        }
    }
}