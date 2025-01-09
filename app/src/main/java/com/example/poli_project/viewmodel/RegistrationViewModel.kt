package com.example.poli_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poli_project.consts.Consts
import com.example.poli_project.database.entity.Patient
import com.example.poli_project.database.repo.UserRepository
import com.example.poli_project.database.entity.Doctor
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState: StateFlow<RegistrationState> = _registrationState
    fun selectTab(index: Int) {
        _registrationState.value = _registrationState.value.copy(selectedTab = index)
    }
    private val _lpuList = MutableStateFlow<List<String>>(emptyList())
    val lpuList: StateFlow<List<String>> = _lpuList

    fun onFieldChange(field: String, value: String) {
        _registrationState.value = when (field) {
            "patientName" -> _registrationState.value.copy(patientName = value)
            "patientSurname" -> _registrationState.value.copy(patientSurname = value)
            "patientMidlname" -> _registrationState.value.copy(patientMidlname = value)
            "patientLogin" -> _registrationState.value.copy(patientLogin = value)
            "patientPassword" -> _registrationState.value.copy(patientPassword = value)
            "doctorPhone" -> _registrationState.value.copy(doctorPhone = value)
            "doctorName" -> _registrationState.value.copy(doctorName = value)
            "doctorSurname" -> _registrationState.value.copy(doctorSurname = value)
            "doctorMidlname" -> _registrationState.value.copy(doctorMidlname = value)
            "doctorLogin" -> _registrationState.value.copy(doctorLogin = value)
            "doctorPassword" -> _registrationState.value.copy(doctorPassword = value)
            "doctorLpu" -> _registrationState.value.copy(doctorLpu = value)
            "doctorSpecialty" -> _registrationState.value.copy(doctorSpecialty = value.uppercase())
            "doctorCategory" -> _registrationState.value.copy(doctorCategory = value.uppercase())
            else -> _registrationState.value
        }
    }

    fun registerNewUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val state = _registrationState.value
            var lpuId = -1
            when(state.doctorLpu){
                "Областная клиническая больница" -> lpuId = 1
                "Городская больница № 4" -> lpuId = 2
                "Городская клиническая больница № 5" -> lpuId = 3
            }
            val result = registerUser(
                isDoctor = state.selectedTab == 1,
                name = if (state.selectedTab == 0) state.patientName else state.doctorName,
                surname = if (state.selectedTab == 0) state.patientSurname else state.doctorSurname,
                midlname = if (state.selectedTab == 0) state.patientMidlname else state.doctorMidlname,
                login = if (state.selectedTab == 0) state.patientLogin else state.doctorLogin,
                password = if (state.selectedTab == 0) state.patientPassword else state.doctorPassword,
                phone = if (state.selectedTab == 1) state.doctorPhone else null,
                specialty = if (state.selectedTab == 1) state.doctorSpecialty else null,
                category = if (state.selectedTab == 1) if (state.doctorCategory == "БЕЗ КАТЕГОРИИ") "БЕЗ_КАТЕГОРИИ" else state.doctorCategory else null,
                doctorLpu = if (state.selectedTab == 1) lpuId else null
            )

            result.onSuccess { response ->
                try {
                    val mapper = jacksonObjectMapper().registerModule(JodaModule())
                    val userId = mapper.readValue<Int>(response)

                    if (userId != -1) {
                        if (state.selectedTab == 0) {
                            val patient = Patient(
                                patientId = userId,
                                name = state.patientName,
                                surname = state.patientSurname,
                                midlname = state.patientMidlname,
                                login = state.patientLogin,
                                password = state.patientPassword
                            )
                            userRepository.insertPatient(patient)
                        } else {
                            val doctor = Doctor(
                                doctorId = userId,
                                name = state.doctorName,
                                surname = state.doctorSurname,
                                midlname = state.doctorMidlname,
                                login = state.doctorLogin,
                                password = state.doctorPassword,
                                phone = state.doctorPhone,
                                speciality = state.doctorSpecialty,
                                category = if (state.doctorCategory == "БЕЗ КАТЕГОРИИ") "БЕЗ_КАТЕГОРИИ" else state.doctorCategory,
                                lpuId = lpuId
                            )
                            userRepository.insertDoctor(doctor)
                        }
                        onSuccess()
                    } else {
                        onError("Ошибка: Не удалось получить ID пользователя")
                    }
                } catch (e: Exception) {
                    onError("Ошибка обработки ответа: ${e.message}")
                }
            }.onFailure {
                onError(it.message ?: "Произошла ошибка")
            }
        }
    }
}


suspend fun registerUser(
    isDoctor: Boolean,
    name: String,
    surname: String,
    midlname: String?,
    login: String,
    password: String,
    phone: String? = null,
    specialty: String? = null,
    category: String? = null,
    doctorLpu: Int?,
): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("name", name)
                put("surname", surname)
                put("midlname", midlname)
                put("login", login)
                put("password", password)
                if (isDoctor) {
                    put("speciality", specialty)
                    put("category", category)
                    put("lpuId", doctorLpu)
                    put("phone", phone)
                }
            }

            val url = if (isDoctor) {
                Consts.URL + "/doctors"
            } else {
                Consts.URL + "/patients"
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(Exception("Ошибка: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



data class RegistrationState(
    val patientName: String = "",
    val patientSurname: String = "",
    val patientMidlname: String = "",
    val patientLogin: String = "",
    val patientPassword: String = "",
    val doctorPhone: String = "",
    val doctorName: String = "",
    val doctorSurname: String = "",
    val doctorMidlname: String = "",
    val doctorLogin: String = "",
    val doctorPassword: String = "",
    val doctorSpecialty: String = "",
    val doctorCategory: String = "",
    val doctorLpu: String = "",
    val selectedTab: Int = 0
) {
    val isFormValid: Boolean
        get() = if (selectedTab == 0) {
            patientName.isNotEmpty() && patientSurname.isNotEmpty() &&
                    patientLogin.isNotEmpty() && patientPassword.isNotEmpty()
        } else {
            doctorName.isNotEmpty() && doctorSurname.isNotEmpty() &&
                    doctorLogin.isNotEmpty() && doctorPassword.isNotEmpty() &&
                    doctorSpecialty.isNotEmpty() && doctorCategory.isNotEmpty() &&
                    doctorPhone.isNotEmpty() && doctorLpu.isNotEmpty()
        }
}