package com.example.suiverliverson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelector() {
    val lang = LocalLang.current
    val setLang = LocalSetLang.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("en" to "English", "fr" to "Français", "ar" to "العربية")
            .forEach { (code, name) ->
                FilterChip(
                    selected = lang == code,
                    onClick = { setLang(code) },
                    label = { Text(name) }
                )
            }
    }
}