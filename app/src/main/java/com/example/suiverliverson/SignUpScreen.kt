package com.example.suiverliverson

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun appFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AppSurface,
    unfocusedContainerColor = AppSurface,
    focusedBorderColor = Red,
    unfocusedBorderColor = DarkGray,
    focusedTextColor = White,
    unfocusedTextColor = White,
    cursorColor = Red
)

@Composable
fun SignUpScreen(store: AccountStore, onBack: () -> Unit, onSuccess: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("B") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val colors = appFieldColors()
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = White)
            }
            Text(
                s.createAccount,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))

        // اختيار الصورة الشخصية
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(AppSurface)
                .border(2.dp, Red, CircleShape)
                .clickable { photoLauncher.launch("image/*") }
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                val bitmap = remember(imageUri) {
                    try {
                        context.contentResolver.openInputStream(imageUri!!).use {
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (e: Exception) {
                        null
                    }
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Red)
                    Spacer(Modifier.height(4.dp))
                    Text("الصورة", color = LightGray, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.lastName, color = LightGray) },
                singleLine = true,
                shape = shape,
                colors = colors
            )
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.firstName, color = LightGray) },
                singleLine = true,
                shape = shape,
                colors = colors
            )
        }
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) age = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(s.age, color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = LightGray) },
            singleLine = true,
            shape = shape,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(14.dp))

        Text(s.licenseType, color = LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A", "B", "C", "D").forEach { type ->
                FilterChip(
                    selected = license == type,
                    onClick = { license = type },
                    label = { Text(type) }
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(s.emailPhone, color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = LightGray) },
            singleLine = true,
            shape = shape,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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
            shape = shape,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(s.confirmPassword, color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = LightGray) },
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            shape = shape,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Red, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val ageInt = age.toIntOrNull()
                error = when {
                    firstName.isBlank() || lastName.isBlank() || age.isBlank() ||
                            login.isBlank() || password.isBlank() || confirm.isBlank() ->
                        s.fillAllFields
                    ageInt == null || ageInt !in 18..70 -> s.invalidAge
                    password.length < 6 -> s.passwordShort
                    password != confirm -> s.passwordMismatch
                    store.exists(login) -> s.accountExists
                    else -> null
                }
                if (error == null && ageInt != null) {
                    val account = store.register(
                        firstName, lastName, ageInt, license, login, password, imageUri?.toString()
                    )
                    store.setCurrent(account)
                    onSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Red)
        ) {
            Text(s.createAccount, fontSize = 17.sp, color = White)
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(s.haveAccount, color = LightGray, fontSize = 14.sp)
            TextButton(onClick = onBack) {
                Text(s.login, color = Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}