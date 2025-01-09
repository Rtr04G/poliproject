package com.example.poli_project.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.poli_project.components.ScreenWithBackButton
import com.example.poli_project.model.Appointment
import com.example.poli_project.model.Patient
import com.example.poli_project.model.Schedule
import com.example.poli_project.viewmodel.DoctorScheduleViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoctorScheduleScreen(
    doctorId: Int,
    onBack: () -> Unit,
    navController: NavController,
    doctorScheduleViewModel: DoctorScheduleViewModel = viewModel()
) {
    LaunchedEffect(doctorId) {
        doctorScheduleViewModel.loadSchedule(doctorId)
    }

    val schedule = doctorScheduleViewModel.schedule
    val isLoading = doctorScheduleViewModel.isLoading
    val errorMessage = doctorScheduleViewModel.errorMessage

    if (isLoading) {
        CircularProgressIndicator()
    } else if (errorMessage != null) {
        Text("Ошибка: $errorMessage")
    } else {
        schedule?.let {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Заявка") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) {
                paddingValues ->
                LazyColumn(
                    modifier = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    val groupedAppointments = schedule.appointments.groupBy {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
                    }

                    groupedAppointments.forEach { (date, appointments) ->
                        item {
                            Text(
                                text = "Дата: $date",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(appointments) { appointment ->
                            AppointmentItem(appointment, schedule.patients, navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: Appointment,
    patients: Map<String, Patient>,
    navController: NavController
) {
    val patient = remember(appointment.patientId) {
        patients[appointment.patientId.toString()]
    }
    val patientJson = remember(appointment.patientId){
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        objectMapper.writeValueAsString(patient)
    }
    val appointmentJson = remember(appointment) {
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        objectMapper.writeValueAsString(appointment)
    }

    val onClick = {
        navController.navigate("appointment_details/${appointmentJson}/$patientJson")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Время: ${formatTime(appointment.date)}",
                style = MaterialTheme.typography.bodyLarge
            )
            if (patient != null) {
                Text(
                    text = "Пациент: ${patient.surname} ${patient.name} ${patient.midlname}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Пациент: ???",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = "Статус: ${appointment.status}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}
