package com.example.suiverliverson

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.suiverliverson.ui.theme.LiversonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContent {
            var lang by remember {
                mutableStateOf(prefs.getString("lang", "en") ?: "en")
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

            CompositionLocalProvider(
                LocalStrings provides strings,
                LocalLang provides lang,
                LocalSetLang provides setLang,
                LocalLayoutDirection provides direction
            ) {
                LiversonTheme {
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