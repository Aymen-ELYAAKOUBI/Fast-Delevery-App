package com.example.suiverliverson

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun ForgotPasswordScreen(store: AccountStore, onBack: () -> Unit) {
    val s = LocalStrings.current
    var login by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

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
                s.resetPassword,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))

        if (done) {
            Text(s.resetDone, color = Green, fontSize = 16.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text(s.login, fontSize = 17.sp, color = White)
            }
        } else {
            Text(s.resetHint, color = LightGray, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))

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
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.lastName, color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = LightGray) },
                singleLine = true,
                shape = shape,
                colors = colors
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) age = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.age, color = LightGray) },
                singleLine = true,
                shape = shape,
                colors = colors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(s.newPassword, color = LightGray) },
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
                        login.isBlank() || lastName.isBlank() || age.isBlank() ||
                                newPassword.isBlank() || confirm.isBlank() -> s.fillAllFields
                        newPassword.length < 6 -> s.passwordShort
                        newPassword != confirm -> s.passwordMismatch
                        ageInt == null ||
                                !store.resetPassword(login, lastName, ageInt, newPassword) ->
                            s.resetFailed
                        else -> null
                    }
                    if (error == null) done = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text(s.resetPassword, fontSize = 17.sp, color = White)
            }
        }
    }
}