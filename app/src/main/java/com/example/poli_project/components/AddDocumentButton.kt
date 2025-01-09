package com.example.poli_project.components

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.poli_project.viewmodel.DocumentViewModel
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddDocumentButton(patientId: Int, doctorId: Int, model: DocumentViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val file = uriToFile(uri, context)
                if (file != null) {
                    model.uploadNewDocument(patientId, doctorId, file)
                } else {
                    Toast.makeText(context, "Не удалось преобразовать файл", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Button(onClick = { launcher.launch("*/*") }) {
        Text("Добавить документ")
    }
}

fun uriToFile(uri: Uri, context: Context): File? {
    val fileName = getFileNameFromUri(uri, context) ?: return null
    return try {
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getFileNameFromUri(uri: Uri, context: Context): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && it.moveToFirst()) {
            it.getString(nameIndex)
        } else null
    }
}

