package com.example.poli_project.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poli_project.components.DropdownMenu
import com.example.poli_project.consts.Consts
import com.example.poli_project.model.CategoryEnum
import com.example.poli_project.model.Doctor
import com.example.poli_project.model.LPU
import com.example.poli_project.model.SpecialityEnum
import com.example.poli_project.model.parseDoctors
import com.example.poli_project.model.parseLPUs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.poli_project.logic.*
import com.example.poli_project.viewmodel.DoctorsViewModel
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen(onBack: () -> Unit, onDoctorSelected: (Int) -> Unit) {
    val doctorsViewModel: DoctorsViewModel = viewModel()
    val doctorsState = doctorsViewModel.doctorsState.collectAsState()
    val lpusState = doctorsViewModel.lpusState.collectAsState()

    val selectedSpeciality = remember { mutableStateOf("") }
    val selectedCategory = remember { mutableStateOf("") }
    val selectedLpu = remember { mutableStateOf("") }
    val searchQuery = remember { mutableStateOf("") }
    val selectedLpuId = remember { mutableIntStateOf(0) }
    Column {
        FilterBar(
            lpus = lpusState.value,
            specialities = doctorsState.value.map { it.speciality }.distinct(),
            categories = doctorsState.value.map { it.category.replace('_',' ') }.distinct(),
            selectedSpeciality = selectedSpeciality,
            selectedCategory = selectedCategory,
            selectedLpu = selectedLpu,
            searchQuery = searchQuery,
            selectedLpuId = selectedLpuId
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                doctorsState.value.filter {
                    (selectedSpeciality.value.isEmpty() || it.speciality == selectedSpeciality.value) &&
                            (selectedCategory.value.isEmpty() || it.category == selectedCategory.value) &&
                            (selectedLpu.value.isEmpty() || it.lpuId == selectedLpuId.value) &&
                            (searchQuery.value.isEmpty() || "${it.surname} ${it.name} ${it.midlname}".contains(searchQuery.value, true))
                }
            ) { doctor ->
                DoctorCard(doctor = doctor, onDoctorSelected = { onDoctorSelected(doctor.doctorId) })
            }
        }
    }
}

@Composable
fun FilterBar(
    lpus: List<LPU>,
    specialities: List<String>,
    categories: List<String>,
    selectedSpeciality: MutableState<String>,
    selectedCategory: MutableState<String>,
    selectedLpu: MutableState<String>,
    searchQuery: MutableState<String>,
    selectedLpuId: MutableState<Int>
) {
    Column {
        DropdownMenu(
            items = specialities,
            label = "Специальность",
            selectedItem = selectedSpeciality.value,
            onItemSelected = { selectedSpeciality.value = it }
        )
        DropdownMenu(
            items = categories,
            label = "Категория",
            selectedItem = selectedCategory.value,
            onItemSelected = { selectedCategory.value = if (it == "БЕЗ КАТЕГОРИИ") "БЕЗ_КАТЕГОРИИ" else it }
        )
        DropdownMenu(
            items = lpus.map { it.address.uppercase() },
            label = "ЛПУ",
            selectedItem = selectedLpu.value,
            onItemSelected = {
                selectedLpu.value = it
                var id = 0
                for (lpu in lpus){
                    if (lpu.address.uppercase() == selectedLpu.value){
                        id = lpu.lpuId
                        break
                    }
                }
                selectedLpuId.value = id
            }
        )
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Поиск по ФИО") }
        )
    }
}

@Composable
fun DoctorCard(doctor: Doctor, onDoctorSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onDoctorSelected() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${doctor.surname} ${doctor.name} ${doctor.midlname}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Врач-${doctor.speciality.lowercase()}, ${doctor.category.replace('_',' ').lowercase()}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { onDoctorSelected() }) {
                Text("Подробнее")
            }
        }
    }
}
