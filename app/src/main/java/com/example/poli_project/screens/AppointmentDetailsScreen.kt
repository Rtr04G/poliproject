package com.example.poli_project.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poli_project.components.AddDocumentButton
import com.example.poli_project.model.Appointment
import com.example.poli_project.model.Document
import com.example.poli_project.model.Patient
import com.example.poli_project.viewmodel.DocumentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppointmentDetailsScreen(
    appointment: Appointment,
    onBack: () -> Unit,
    patient: Patient
) {
    val documentViewModel: DocumentViewModel = viewModel()
    val documents by documentViewModel.documents
    val isLoading = documentViewModel.isLoading.value
    val errorMessage = documentViewModel.errorMessage.value
    val context = LocalContext.current

    LaunchedEffect(appointment.patientId) {
        documentViewModel.loadDocuments(appointment.patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заявка№${appointment.appointmentId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = "Пациент: ${patient.surname+" "+patient.name+" "+patient.midlname}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AddDocumentButton(
                    patientId = appointment.patientId,
                    doctorId = appointment.doctorId,
                    model = documentViewModel
                )
                Column(modifier = Modifier.weight(32f)) {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                documentViewModel.complApp(appointment)
                            }
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Закрыть заявку")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch { documentViewModel.calApp(appointment) }
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Отменить заявку")
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Документы:", style = MaterialTheme.typography.bodyLarge)
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text("Ошибка: $errorMessage", color = MaterialTheme.colorScheme.error)
            } else if (documents.isEmpty()) {
                Text("Документы не найдены.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents) { document ->
                        DocumentRow(document = document) { doc ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = documentViewModel.downloadDocument(doc.documentId, context)
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        Toast.makeText(context, "Документ загружен", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Ошибка загрузки документа", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun DocumentRow(document: Document, onDownload: (Document) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Скачивание документа") },
            text = { Text("Вы действительно хотите скачать документ \"${document.src}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDownload(document)
                }) {
                    Text("Скачать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = document.src,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Uploaded: ${document.createAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
