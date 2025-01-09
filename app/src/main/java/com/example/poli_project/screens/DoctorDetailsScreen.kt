package com.example.poli_project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.poli_project.model.Doctor
import com.example.poli_project.model.LPU

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailsScreen(
    doctor: Doctor,
    lpus: List<LPU>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onBookAppointment: () -> Unit
) {
    val lpuAddress = lpus.firstOrNull { it.lpuId == doctor.lpuId }?.address ?: "Неизвестно"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text("Информация о враче") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )

        Text(
            text = "${doctor.surname} ${doctor.name} ${doctor.midlname}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Panel(
                title = "Специальность",
                content = doctor.speciality,
                modifier = Modifier.weight(1f)
            )
            Panel(
                title = "Категория",
                content = doctor.category,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Panel(
                title = "ЛПУ",
                content = lpuAddress,
                modifier = Modifier.weight(1f)
            )
            Panel(
                title = "Контакты",
                content = doctor.phone,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { onBookAppointment() },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Выбрать дату и время")
        }
    }
}
@Composable
fun Panel(title: String, content: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp)
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoctorDetailsScreen() {
    val doctor = Doctor(
        doctorId = 1,
        name = "Иван",
        midlname = "Иванович",
        surname = "Иванов",
        phone = "+7 (123) 456-78-90",
        speciality = "Терапевт",
        category = "Высшая",
        lpuId = 3
    )
    val lpus = listOf(
        LPU(lpuId = 1, address = "Областная клиническая больница", phone = "+7 (4922) 40-71-02"),
        LPU(lpuId = 3, address = "Городская больница №4", phone = "+7 (4922) 32-47-49")
    )

    DoctorDetailsScreen(
        doctor = doctor,
        lpus = lpus,
        onBack = {},
        onLogout = {},
        onBookAppointment = {}
    )
}