package com.example.poli_project.viewmodel

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poli_project.logic.copmliteApp
import com.example.poli_project.logic.downDocument
import com.example.poli_project.logic.dropApp
import com.example.poli_project.logic.getDocuments
import com.example.poli_project.logic.uploadDocument
import com.example.poli_project.model.Appointment
import com.example.poli_project.model.Document
import kotlinx.coroutines.launch
import java.io.File

class DocumentViewModel : ViewModel() {
    var documents = mutableStateOf<List<Document>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadDocuments(patientId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val result = getDocuments(patientId)
                documents.value = result ?: emptyList()
                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = e.message
                isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadNewDocument(patientId: Int, doctorId: Int, file: File?) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = uploadDocument(patientId, doctorId, file)
                if (success) {
                    loadDocuments(patientId)
                }
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    suspend fun complApp(appointment: Appointment){
        copmliteApp(appointment)
    }

    suspend fun calApp(appointment: Appointment){
        dropApp(appointment)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun downloadDocument(documentId: Int, context: Context): Boolean {
        return try {
            val response = downDocument(documentId)
            if (response?.isSuccessful == true) {
                val fileName = response.headers["Content-Disposition"]
                    ?.substringAfter("filename=")
                    ?.substringAfterLast("''")
                    ?.decodeURL()
                    ?: "document_$documentId"

                val resolver = context.contentResolver
                val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, response.headers["Content-Type"] ?: "application/octet-stream")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val fileUri = resolver.insert(downloadsUri, contentValues)
                if (fileUri != null) {
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        response.body?.byteStream()?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(fileUri, contentValues, null, null)

                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun String.decodeURL(): String = java.net.URLDecoder.decode(this, "UTF-8")

}

