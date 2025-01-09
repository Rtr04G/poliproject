package com.example.poli_project.logic

import com.example.poli_project.consts.Consts
import com.example.poli_project.model.Document
import com.example.poli_project.model.DocumentsResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

suspend fun downDocument(documentId: Int): Response? {
    val client = OkHttpClient()
    val requestForDocuments = Request.Builder()
        .url(Consts.URL + "/documents/download/" + documentId)
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(requestForDocuments).execute()
            response
        } catch (e: IOException) {
            null
        }
    }
}