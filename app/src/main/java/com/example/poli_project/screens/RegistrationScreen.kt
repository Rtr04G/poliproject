package com.example.poli_project.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poli_project.viewmodel.RegistrationViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.poli_project.database.AppDatabase
import com.example.poli_project.database.repo.UserRepository
import com.example.poli_project.viewmodel.RegistrationState

@Composable
fun RegistrationScreen(
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    context: Context
) {
    val userRepository = UserRepository(AppDatabase.getDatabase(context).userDao())
    val viewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModelFactory(userRepository))
    val state by viewModel.registrationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = state.selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Пациент") }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Врач") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedTab == 0) {
            PatientRegistrationForm(state, viewModel::onFieldChange)
        } else {
            DoctorRegistrationForm(state, viewModel::onFieldChange)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.registerNewUser(
                    onSuccess = { onRegistrationSuccess() },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = state.isFormValid,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}

@Composable
fun PatientRegistrationForm(
    state: RegistrationState,
    onFieldChange: (String, String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = state.patientSurname,
            onValueChange = { onFieldChange("patientSurname", it) },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.patientName,
            onValueChange = { onFieldChange("patientName", it) },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.patientMidlname,
            onValueChange = { onFieldChange("patientMidlname", it) },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.patientLogin,
            onValueChange = { onFieldChange("patientLogin", it) },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.patientPassword,
            onValueChange = { onFieldChange("patientPassword", it) },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun DoctorRegistrationForm(
    state: RegistrationState,
    onFieldChange: (String, String) -> Unit
) {
    val lpuList = listOf(
        "Областная клиническая больница",
        "Городская больница № 4",
        "Городская клиническая больница № 5"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = state.doctorSurname,
            onValueChange = { onFieldChange("doctorSurname", it) },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.doctorName,
            onValueChange = { onFieldChange("doctorName", it) },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.doctorMidlname,
            onValueChange = { onFieldChange("doctorMidlname", it) },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.doctorLogin,
            onValueChange = { onFieldChange("doctorLogin", it) },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.doctorPassword,
            onValueChange = { onFieldChange("doctorPassword", it) },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenu(
            items = lpuList,
            label = "LPU",
            selectedItem = state.doctorLpu,
            onItemSelected = { onFieldChange("doctorLpu", it) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenu(
            items = listOf("ТЕРАПЕВТ", "КАРДИОЛОГ", "ПУЛЬМОНОЛОГ", "НЕВРОЛОГ"),
            label = "Специальность",
            selectedItem = state.doctorSpecialty,
            onItemSelected = { onFieldChange("doctorSpecialty", it) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenu(
            items = listOf("ВЫСШАЯ", "ПЕРВАЯ", "ВТОРАЯ", "БЕЗ КАТЕГОРИИ"),
            label = "Категория",
            selectedItem = state.doctorCategory,
            onItemSelected = { onFieldChange("doctorCategory", it) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.doctorPhone,
            onValueChange = { onFieldChange("doctorPhone", it) },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    items: List<String>,
    label: String,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(0.8f),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
class RegistrationViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
