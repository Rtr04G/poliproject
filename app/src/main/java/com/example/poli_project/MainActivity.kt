package com.example.poli_project
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poli_project.model.Appointment
import com.example.poli_project.model.Patient
import com.example.poli_project.screens.AddTemplateScreen
import com.example.poli_project.screens.AppointmentDetailsScreen
import com.example.poli_project.screens.AppointmentScreen
import com.example.poli_project.screens.BookingScreen
import com.example.poli_project.screens.DoctorDetailsScreen
import com.example.poli_project.screens.DoctorMainScreen
import com.example.poli_project.screens.DoctorScheduleScreen
import com.example.poli_project.screens.GettingStartedScreen
import com.example.poli_project.screens.LoginScreen
import com.example.poli_project.screens.MainScreen
import com.example.poli_project.screens.MedicalCardScreen
import com.example.poli_project.screens.RegistrationScreen
import com.example.poli_project.screens.ScheduleScreen
import com.example.poli_project.viewmodel.DoctorsViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val doctorsViewModel: DoctorsViewModel = viewModel()
    val doctorsState = doctorsViewModel.doctorsState.collectAsState()
    var doctorIdd = -1
    var patientIdd = -1
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { isDoctor, id ->
                    navController.navigate(if (isDoctor) "doctor_main" else "patient_main")
                    if (isDoctor){
                        doctorIdd = id
                    }
                    else{
                        patientIdd = id
                    }
                },
                onLoginError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                },
                onRegister = {navController.navigate("register")}
            )
        }

        composable("register") {
            RegistrationScreen(
                context = context,
                onNavigateToLogin = { navController.navigate("login") },
                onRegistrationSuccess = {
                    Toast.makeText(context, "Вы зарегистрированы!", Toast.LENGTH_SHORT).show()
                    navController.navigate("login")
                }
            )
        }

        composable("patient_main") {
            MainScreen(
                onNavigateToProfile = { navController.navigate("getting_started") },
                onNavigateToMedicalCard = { navController.navigate("medical_card") },
                onNavigateToAppointment = { navController.navigate("appointment") },
                onNavigateToSchedule = { navController.navigate("patient_schedule") },
                onLogout = { navController.navigate("login") { popUpTo("login") } }
            )
        }
        composable("doctor_main") {
            DoctorMainScreen(
                onNavigateToProfile = { navController.navigate("getting_started") },
                onNavigateToSchedule = { navController.navigate("doctor_schedule/$doctorIdd") },
                onNavigateToAddTemplate = { navController.navigate("add_template") },
                onLogout = { navController.navigate("login") { popUpTo("login") } }
            )
        }
        composable("medical_card") { MedicalCardScreen(onBack = { navController.popBackStack() }) }
        composable("appointment") {
            AppointmentScreen(
                onBack = { navController.popBackStack() },
                onDoctorSelected = { doctorId ->
                    navController.navigate("doctor_details/$doctorId")
                }
            )
        }

        composable("doctor_details/{doctorId}") { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId")?.toInt() ?: return@composable
            val doctor = doctorsState.value.firstOrNull { it.doctorId == doctorId }

            if (doctor != null) {
                DoctorDetailsScreen(
                    doctor = doctor,
                    lpus = doctorsViewModel.lpusState.collectAsState().value,
                    onBack = { navController.popBackStack() },
                    onLogout = { navController.navigate("login") { popUpTo("login") } },
                    onBookAppointment = {
                        navController.navigate("booking_screen/$doctorId")
                    }
                )
            } else {
                Toast.makeText(context, "Доктор не найден", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

        composable("patient_schedule") { ScheduleScreen(onBack = { navController.popBackStack() }) }

        composable("doctor_schedule/{doctorId}") { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId")?.toInt() ?: return@composable
            DoctorScheduleScreen(
                doctorId = doctorId,
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("appointment_details/{appointmentJson}/{patientJson}") { backStackEntry ->
            val appointmentJson = backStackEntry.arguments?.getString("appointmentJson")
            val patientJson =  backStackEntry.arguments?.getString("patientJson")
            if (appointmentJson != null && patientJson != null) {
                val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
                val appointment = objectMapper.readValue(appointmentJson, Appointment::class.java)
                val patient = objectMapper.readValue(patientJson, Patient::class.java)
                AppointmentDetailsScreen(
                    patient = patient,
                    appointment = appointment,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("add_template") { AddTemplateScreen(onBack = { navController.popBackStack() }) }

        composable("booking_screen/{doctorId}") { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId")?.toInt() ?: return@composable

            BookingScreen(
                patientId = patientIdd,
                doctorId = doctorId,
                onBack = { navController.popBackStack() },
                onBookingSuccess = {}
            )
        }
        composable("getting_started") {
            GettingStartedScreen(onBack = { navController.popBackStack() })
        }
    }
}





