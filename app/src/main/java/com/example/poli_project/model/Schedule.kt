package com.example.poli_project.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

data class Schedule(
    val patients: Map<String, Patient>,
    val appointments: List<Appointment>
) {
    fun toJsonString(): String {
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        return objectMapper.writeValueAsString(this)
    }
}