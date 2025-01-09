package com.example.poli_project.model

data class DocumentRecord(
    val recordId: Int,
    val patientId: Int,
    val src: String,
    val createAt: Long
)

data class Document(
    val documentId: Int,
    val recordId: Int,
    val doctorId: Int,
    val src: String,
    val createAt: Long
)

data class DocumentsResponse(
    val record: DocumentRecord,
    val documents: List<Document>
)
