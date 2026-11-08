package com.example.suiverliverson

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowInsetsControllerCompat
import com.example.suiverliverson.ui.theme.LiversonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContent {
            var lang by remember {
                mutableStateOf(prefs.getString("lang", "en") ?: "en")
            }
            var darkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", true))
            }
            var orderNotificationsEnabled by remember {
                mutableStateOf(prefs.getBoolean("order_notifications_enabled", false))
            }
            val setDarkMode: (Boolean) -> Unit = { enabled ->
                darkMode = enabled
                prefs.edit().putBoolean("dark_mode", enabled).apply()
            }
            val setOrderNotificationsEnabled: (Boolean) -> Unit = { enabled ->
                orderNotificationsEnabled = enabled
                prefs.edit().putBoolean("order_notifications_enabled", enabled).apply()
                if (!enabled) OrderNotificationManager.cancelAll(this@MainActivity)
            }
            val setLang: (String) -> Unit = {
                lang = it
                prefs.edit().putString("lang", it).apply()
            }
            val strings = when (lang) {
                "ar" -> ArStrings
                "fr" -> FrStrings
                else -> EnStrings
            }
            val direction =
                if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            SideEffect {
                val systemBarColor = if (darkMode) AndroidColor.rgb(12, 20, 33) else AndroidColor.WHITE
                window.statusBarColor = systemBarColor
                window.navigationBarColor = systemBarColor
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkMode
                    isAppearanceLightNavigationBars = !darkMode
                }
            }

            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalLang provides lang,
                LocalSetLang provides setLang,
                LocalDarkMode provides darkMode,
                LocalSetDarkMode provides setDarkMode,
                LocalOrderNotificationsEnabled provides orderNotificationsEnabled,
                LocalSetOrderNotificationsEnabled provides setOrderNotificationsEnabled,
                LocalLayoutDirection provides direction
            ) {
                LiversonTheme(darkTheme = darkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNav()
                    }
                }
            }
        }
    }
}