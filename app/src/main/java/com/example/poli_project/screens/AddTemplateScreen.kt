package com.example.poli_project.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.poli_project.components.ScreenWithBackButton

@Composable
fun AddTemplateScreen(onBack: () -> Unit) {
    ScreenWithBackButton("Экран добавления шаблона", onBack) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Экран добавления шаблона")
        }
    }
}