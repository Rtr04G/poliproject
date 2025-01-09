package com.example

import com.dao.*
import com.dto.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.io.EOFException
import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:mysql://localhost:3306/poliproject",
        user = "root",
        driver = "com.mysql.cj.jdbc.Driver",
        password = "Tz64-cjZR-j6sz-327j-ryx4-7zWp-v4FG",
    )
    DBService(database)
    embeddedServer(Netty, port = 8020, host = "0.0.0.0") {
        routing {
            route("/patients") {
                post {
                    try {
                        val request = call.receive<String>()
                        val mapper = jacksonObjectMapper().registerModule(JodaModule())
                        val patient = mapper.readValue<PatientDTO>(request)
                        val id = createPatient(
                            name = patient.name,
                            midlname = patient.midlname,
                            surname = patient.surname,
                            login = patient.login,
                            password = Base64.getEncoder().encodeToString(patient.password.toByteArray())
                        )
                        createMedicalRecord(
                            id,
                            "C:\\Users\\rtgr0\\Documents\\db_docs\\m_rec\\$id")
                        println(patient)
                        call.respond(HttpStatusCode.OK, mapper.writeValueAsString(mapper.writeValueAsString(id)))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error creating patient: ${e.message}")
                    }
                }

                get {
                    try {
                        val patients = getAllPatients()
                        call.respond(patients.toString())
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error fetching patients: ${e.message}")
                    }
                }

                get("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                        return@get
                    }
                    try {
                        val patient = getPatientById(id)
                        println(patient?.get("password"))
                        if (patient == null) {
                            call.respond(HttpStatusCode.NotFound, "Patient not found")
                        } else {
                            call.respond(HttpStatusCode.OK, patient.toString())
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error fetching patient: ${e.message}")
                    }
                }

                get("/auth/{username&password}") {
                    val usernameAndPassword =  call.parameters["username&password"]
                    val username = String(Base64.getDecoder().decode(usernameAndPassword)).substringBefore('&')
                    val password = Base64.getEncoder().encodeToString(
                        String(
                            Base64.getDecoder().decode(usernameAndPassword)
                        )
                            .substringAfter('&').toByteArray()
                    )
                    try {
                        if(password != null) {
                            val mapper = jacksonObjectMapper().registerModule(JodaModule())
                            val auth = getPatientAut(username, password)
                            println(auth?.get("login"));
                            if (auth === null) {
                                call.respond(HttpStatusCode.NotFound, "Patient not found")
                            } else {
                                val id = auth.get("patientId")
                                call.respond(HttpStatusCode.OK, mapper.writeValueAsString(mapper.writeValueAsString(id)))
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid password")
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error fetching patient for auth: ${e.message}")
                    }
                }

                put("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                        return@put
                    }
                    try {
                        val updatedPatient = call.receive<PatientDTO>()
                        val updated = updatePatient(
                            patientId = id,
                            name = updatedPatient.name,
                            midlname = updatedPatient.midlname,
                            surname = updatedPatient.surname,
                            login = updatedPatient.login,
                            password = Base64.getEncoder().encodeToString(updatedPatient.password.toByteArray())
                        )
                        if (updated) {
                            call.respond(HttpStatusCode.OK, "Patient updated successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Patient not found")
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error updating patient: ${e.message}")
                    }
                }

                delete("{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                        return@delete
                    }
                    try {
                        val deleted = deletePatient(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, "Patient deleted successfully")
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Patient not found")
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error deleting patient: ${e.message}")
                    }
                }
            }
            route("/doctors") {
                get {
                    val doctors = getAllDoctors()
                    for (doctor in doctors) {
                        doctor.login = ""
                        doctor.password = ""
                    }
                    val result = mapOf("lpus" to getAllLPUs(), "doctors" to doctors)
                    val mapper = ObjectMapper().registerModule(JodaModule())
                    call.respond(HttpStatusCode.OK, mapper.writeValueAsString(result))
                }

                get("/{id}") {
                    val doctorId = call.parameters["id"]?.toIntOrNull()
                    if (doctorId != null) {
                        val doctor = getDoctorById(doctorId)
                        if (doctor != null) {
                            call.respond(doctor)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Doctor not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid doctor ID")
                    }
                }

                get("/schedule/{id}") {
                    val doctorId = call.parameters["id"]?.toIntOrNull()
                    if (doctorId != null) {
                        val appointments = getAppointmentsByDoctorIdToDo(doctorId)
                        val patients = mutableMapOf<Int, MutableMap<String, Any?>?>()
                        for (appointment in appointments) {
                            patients[appointment.patientId] = getPatientById(appointment.patientId)?.toMutableMap()
                            patients[appointment.patientId]?.remove("login")
                            patients[appointment.patientId]?.remove("password")
                        }
                        val resultMap = mutableMapOf<String, Any?>()
                        resultMap["patients"] = patients
                        resultMap["appointments"] = appointments
                        val mapper = jsonMapper().registerModule(JodaModule())
                        val result = mapper.writeValueAsString(resultMap)
                        if (appointments != null) {
                            call.respond(result)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Doctor not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid doctor ID")
                    }
                }


                get("/auth/{username&password}") {
                    val usernameAndPassword =  call.parameters["username&password"]
                    val username = String(Base64.getDecoder().decode(usernameAndPassword)).substringBefore('&')
                    val password = Base64.getEncoder().encodeToString(
                        String(
                            Base64.getDecoder().decode(usernameAndPassword)
                        )
                            .substringAfter('&').toByteArray()
                    )
                    try {
                        if(password != null) {
                            val mapper = jacksonObjectMapper().registerModule(JodaModule())
                            val auth = getDoctorAut(username, password)
                            println(auth?.get("login"));
                            if (auth === null) {

                                call.respond(HttpStatusCode.NotFound, "Patient not found")
                            } else {
                                val id = auth.get("doctorId")
                                call.respond(HttpStatusCode.OK, mapper.writeValueAsString(mapper.writeValueAsString(id)))
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid password")
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error fetching patient for auth: ${e.message}")
                    }
                }

                post {
                    val request = call.receive<String>()
                    val mapper = jacksonObjectMapper().registerModule(JodaModule())
                    val doctor = mapper.readValue<DoctorDTO>(request)
                    val id = createDoctor(
                        doctor.name,
                        doctor.midlname,
                        doctor.surname,
                        doctor.phone,
                        doctor.login,
                        Base64.getEncoder().encodeToString(doctor.password.toByteArray()),
                        doctor.speciality,
                        doctor.category,
                        doctor.lpuId
                    )
                    call.respond(HttpStatusCode.OK, mapper.writeValueAsString(mapper.writeValueAsString(id)))
                }

                put("/{id}") {
                    val doctorId = call.parameters["id"]?.toIntOrNull()
                    val doctor = call.receive<DoctorDTO>()
                    if (doctorId != null && updateDoctor(
                            doctorId,
                            doctor.name,
                            doctor.midlname,
                            doctor.surname,
                            doctor.phone,
                            doctor.login,
                            Base64.getEncoder().encodeToString(doctor.password.toByteArray()),
                            doctor.speciality,
                            doctor.category,
                            doctor.lpuId
                        )
                    ) {
                        call.respond(HttpStatusCode.OK, "Doctor updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid doctor ID or data")
                    }
                }

                delete("/{id}") {
                    val doctorId = call.parameters["id"]?.toIntOrNull()
                    if (doctorId != null && deleteDoctor(doctorId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid doctor ID")
                    }
                }
            }
            route("/templates") {
                get {
                    val templates = getAllTemplates()
                    call.respond(templates)
                }

                get("/{id}") {
                    val templateId = call.parameters["id"]?.toIntOrNull()
                    if (templateId != null) {
                        val template = getTemplateById(templateId)
                        if (template != null) {
                            call.respond(template)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Template not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid template ID")
                    }
                }

                post {
                    val template = call.receive<TemplateDTO>()
                    val templateId = createTemplate(template.src)
                    call.respond(HttpStatusCode.Created, "Template created with ID: $templateId")
                }

                put("/{id}") {
                    val templateId = call.parameters["id"]?.toIntOrNull()
                    val template = call.receive<TemplateDTO>()
                    if (templateId != null && updateTemplate(templateId, template.src)) {
                        call.respond(HttpStatusCode.OK, "Template updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid template ID or data")
                    }
                }

                delete("/{id}") {
                    val templateId = call.parameters["id"]?.toIntOrNull()
                    if (templateId != null && deleteTemplate(templateId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid template ID")
                    }
                }
            }
            route("/analytics") {
                get {
                    val analytics = getAllAnalytics()
                    call.respond(analytics)
                }

                get("/{id}") {
                    val statId = call.parameters["id"]?.toIntOrNull()
                    if (statId != null) {
                        val analytics = getAnalyticsById(statId)
                        if (analytics != null) {
                            call.respond(analytics)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Analytics record not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid stat ID")
                    }
                }

                post {
                    val analytics = call.receive<AnalyticsDTO>()
                    val statId = createAnalytics(analytics.metric, analytics.value)
                    call.respond(HttpStatusCode.Created, "Analytics created with ID: $statId")
                }

                put("/{id}") {
                    val statId = call.parameters["id"]?.toIntOrNull()
                    val analytics = call.receive<AnalyticsDTO>()
                    if (statId != null && updateAnalytics(statId, analytics.metric, analytics.value)) {
                        call.respond(HttpStatusCode.OK, "Analytics updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid stat ID or data")
                    }
                }

                delete("/{id}") {
                    val statId = call.parameters["id"]?.toIntOrNull()
                    if (statId != null && deleteAnalytics(statId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid stat ID")
                    }
                }
            }
            route("/documents") {
                get {
                    val documents = getAllDocuments()
                    call.respond(documents)
                }

                get("/{id}") {
                    val documentId = call.parameters["id"]?.toIntOrNull()
                    if (documentId != null) {
                        val document = getDocumentById(documentId)
                        if (document != null) {
                            call.respond(document)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Document not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid document ID")
                    }
                }

                get("byRecord/{id}") {
                    val recordId = call.parameters["id"]?.toIntOrNull()
                    if (recordId != null) {
                        val documents = getDocumentsByRecordId(recordId)
                        if (documents != null) {
                            call.respond(documents)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Document not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid document ID")
                    }
                }

                post {
                    val document = call.receive<DocumentDTO>()
                    val documentId = createDocument(document.recordId, document.doctorId, document.src)
                    call.respond(HttpStatusCode.Created, "Document created with ID: $documentId")
                }


                post("/upload") {
                    val multipart = call.receiveMultipart()
                    var uploadedFileName: String? = null
                    var patientId: Int? = null
                    var doctorId: Int? = null

                    try {
                        while (true) {
                            val part = multipart.readPart()
                            when (part) {
                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "patientId" -> patientId = part.value.toIntOrNull()
                                        "doctorId" -> doctorId = part.value.toIntOrNull()
                                    }
                                }

                                is PartData.FileItem -> {
                                    val fileName = part.originalFileName ?: "uploaded_file.docx"
                                    val fileBytes = part.streamProvider().readBytes()
                                    val filePath = "C:\\Users\\rtgr0\\Documents\\db_docs\\m_rec\\$patientId\\$fileName"
                                    File(filePath).writeBytes(fileBytes)

                                    uploadedFileName = fileName
                                }

                                else -> {}
                            }

                            part?.dispose?.let { it() }
                            if (part == null){
                                break
                            }
                        }
                    } catch (_: EOFException) {
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, "Error during file upload: ${e.message}")
                        return@post
                    }

                    if (uploadedFileName != null && patientId != null && doctorId != null) {
                        getMedicalRecordByPatientId(patientId)?.let {
                            createDocument(
                                it.recordId,
                                doctorId, uploadedFileName)
                        }
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                get("/download/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    val document = id?.let { getDocumentById(it) }
                    val record = document?.let { getMedicalRecordById(it.recordId) }
                    if (id != null && document != null && record != null) {
                        val filePath = File(record.src + document.src)
                        if (filePath.exists()) {
                            val encodedFileName = java.net.URLEncoder.encode(document.src, Charsets.UTF_8.toString()).replace("+", "%20")

                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(
                                    ContentDisposition.Parameters.FileName, document.src
                                ).toString() + "; filename*=UTF-8''$encodedFileName"
                            )
                            call.respondFile(filePath)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "File not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "File name missing")
                    }
                }


                put("/{id}") {
                    val documentId = call.parameters["id"]?.toIntOrNull()
                    val document = call.receive<DocumentDTO>()
                    if (documentId != null && updateDocument(documentId, document.src)) {
                        call.respond(HttpStatusCode.OK, "Document updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid document ID or data")
                    }
                }

                delete("/{id}") {
                    val documentId = call.parameters["id"]?.toIntOrNull()
                    if (documentId != null && deleteDocument(documentId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid document ID")
                    }
                }
            }
            route("/appointments") {
                get {
                    val appointments = getAllAppointments()
                    call.respond(appointments)
                }

                get("{id}") {
                    val appointmentId = call.parameters["id"]?.toIntOrNull()
                    if (appointmentId != null) {
                        val appointment = getAppointmentById(appointmentId)
                        if (appointment != null) {
                            val mapper = ObjectMapper().registerModule(JodaModule())
                            call.respond(HttpStatusCode.OK, mapper.writeValueAsString(appointment))
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Appointment not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid appointment ID")
                    }
                }

                get("/doctor_schedule/{id}") {
                    val doctorId = call.parameters["id"]?.toIntOrNull()
                    if (doctorId != null) {
                        val appointments = getAppointmentsByDoctorId(doctorId)
                        val result1 = mutableMapOf<String, String>()
                        var i = 0
                        for (appointment in appointments) {
                            if(appointment.date.toLocalDate() > DateTime.now().toLocalDate()) {
                                result1["datetime$i"] = appointment.date.toString()
                                i++
                            }
                        }
                        val result2 = convertMapToJson(result1, "datatimes")
                        call.respond(HttpStatusCode.OK, result2)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid appointment ID")
                    }
                }

                get("/schedule/{id}") {
                    val appointmentId = call.parameters["id"]?.toIntOrNull()
                    if (appointmentId != null) {
                        val appointment = getScheduleById(appointmentId)
                        if (appointment != null) {
                            val mapper = ObjectMapper().registerModule(JodaModule())
                            call.respond(HttpStatusCode.OK, mapper.writeValueAsString(appointment))
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Appointment not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid appointment ID")
                    }
                }

                post {
                    val mapper = jacksonObjectMapper().registerModule(JodaModule())
                    val mut = call.receive<String>()
                    val appointment = mapper.readValue<AppointmentDTO>(mut)

                    val appointmentId = createAppointment(
                        patientId = appointment.patientId,
                        doctorId = appointment.doctorId,
                        date = DateTime(appointment.date).minusHours(3),
                        status = AppointmentStatus.valueOf(appointment.status)
                    )

                    call.respond(HttpStatusCode.Created, "Appointment created with ID: $appointmentId")
                }


                post("/update/") {
                    val mapper = jacksonObjectMapper().registerModule(JodaModule())
                    val str = call.receive<String>()
                    val appointment = mapper.readValue<AppointmentDTO>(str)
                    if (appointment.appointmentId != null && updateAppointment(
                            appointment.appointmentId,
                            appointment.date,
                            AppointmentStatus.valueOf(appointment.status)
                        )
                    ) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid appointment ID or data")
                    }
                }

                delete("/{id}") {
                    val appointmentId = call.parameters["id"]?.toIntOrNull()
                    if (appointmentId != null && deleteAppointment(appointmentId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid appointment ID")
                    }
                }
            }
            route("/lpus") {
                get {
                    val lpus = getAllLPUs()
                    call.respond(lpus)
                }

                get("/{id}") {
                    val lpuId = call.parameters["id"]?.toIntOrNull()
                    if (lpuId != null) {
                        val lpu = getLPUsById(lpuId)
                        if (lpu != null) {
                            call.respond(lpu)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "LPU not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid LPU ID")
                    }
                }

                post {
                    val lpu = call.receive<LPUDTO>()
                    val lpuId = createLPUs(lpu.address, lpu.phone)
                    call.respond(HttpStatusCode.Created, "LPU created with ID: $lpuId")
                }

                put("/{id}") {
                    val lpuId = call.parameters["id"]?.toIntOrNull()
                    val lpu = call.receive<LPUDTO>()
                    if (lpuId != null && updateLPUs(lpuId, lpu.address, lpu.phone)) {
                        call.respond(HttpStatusCode.OK, "LPU updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid LPU ID or data")
                    }
                }

                delete("/{id}") {
                    val lpuId = call.parameters["id"]?.toIntOrNull()
                    if (lpuId != null && deleteLPUs(lpuId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid LPU ID")
                    }
                }
            }
            route("/medical-records") {

                get {
                    val records = getAllMedicalRecords()
                    call.respond(records)
                }

                get("/{id}") {
                    val patientId = call.parameters["id"]?.toIntOrNull()
                    if (patientId != null) {
                        val record = getMedicalRecordByPatientId(patientId)
                        val documents = record?.let { getDocumentsByRecordId(it.recordId) }
                        if (record != null) {
                            val mapper = jacksonObjectMapper().registerModule(JodaModule())
                            val result = mutableMapOf<String, Any?>()
                            result["record"] = record
                            result["documents"] = documents
                            val json = mapper.writeValueAsString(result)
                            call.respond(HttpStatusCode.OK, json)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Medical record not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid record ID")
                    }
                }

                post {
                    val record = call.receive<MedicalRecordDTO>()
                    val recordId = createMedicalRecord(record.patientId, record.src)
                    call.respond(HttpStatusCode.Created, "Medical record created with ID: $recordId")
                }

                put("/{id}") {
                    val recordId = call.parameters["id"]?.toIntOrNull()
                    val record = call.receive<MedicalRecordDTO>()
                    if (recordId != null && updateMedicalRecord(recordId, record.src)) {
                        call.respond(HttpStatusCode.OK, "Medical record updated")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid record ID or data")
                    }
                }

                delete("/{id}") {
                    val recordId = call.parameters["id"]?.toIntOrNull()
                    if (recordId != null && deleteMedicalRecord(recordId)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid record ID")
                    }
                }
            }
        }
    }.start(wait = true)
}

fun convertMapToJson(map: MutableMap<String, String>, arrayName: String): String {
    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    val jsonArray = map.map { mapOf(it.key to it.value) }

    val result = mapOf(arrayName to jsonArray)

    return objectMapper.writeValueAsString(result)
}

suspend fun writeFileAsync(filePath: String, fileBytes: ByteArray, callback: (Boolean) -> Unit) {
    suspendCoroutine { continuation ->
        try {
            File(filePath).writeBytes(fileBytes)
            continuation.resume(Unit)
            callback(true)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
            callback(false)
        }
    }
}