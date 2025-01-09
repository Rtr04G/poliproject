package com.dto

data class DocumentDTO(
    val documentId: Int,
    val recordId: Int,
    val doctorId: Int,
    val src: String,
    val createAt: org.joda.time.DateTime
)
