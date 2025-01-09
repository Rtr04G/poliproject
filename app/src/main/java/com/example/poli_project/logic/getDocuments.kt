package com.example.poli_project.logic

import com.example.poli_project.model.DocumentsResponse
import com.example.poli_project.consts.Consts
import com.example.poli_project.model.Document
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

suspend fun getDocuments(patientId: Int): List<Document>? {
    val client = OkHttpClient()
    val requestForDocuments = Request.Builder()
        .url(Consts.URL + "/medical-records/" + patientId)
        .build()

    return withContext(Dispatchers.IO) {
            try {
            val response = client.newCall(requestForDocuments).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody).toString()
                    val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
                    val documentsResponse = objectMapper.readValue(json, DocumentsResponse::class.java)
                    documentsResponse.documents
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }
}
