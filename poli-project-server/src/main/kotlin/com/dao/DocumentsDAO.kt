package com.dao

import com.dto.DocumentDTO
import com.example.DBService.Documents
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun createDocument(recordId: Int, doctorId: Int, src: String): Any {
    val docs = transaction { Documents.selectAll().where { (Documents.recordId eq recordId) and (Documents.doctorId eq doctorId) and (Documents.src eq src)}
        .map {
            DocumentDTO(
                documentId = it[Documents.documentId],
                recordId = it[Documents.recordId],
                doctorId = it[Documents.doctorId],
                src = it[Documents.src],
                createAt = it[Documents.createAt]
            )
        }
        .singleOrNull()
    }
    if (docs == null) {
        return transaction {
            Documents.insert {
                it[Documents.recordId] = recordId
                it[Documents.doctorId] = doctorId
                it[Documents.src] = src
                it[Documents.createAt] = org.joda.time.DateTime.now()
                it[Documents.updateAt] = org.joda.time.DateTime.now()
            } get Documents.documentId
        }
    }
    else{
        return transaction {
            Documents.update({ Documents.documentId eq docs.documentId }) {
                it[Documents.updateAt] = org.joda.time.DateTime.now()
            } > 0
        }
    }
}

fun getAllDocuments(): List<DocumentDTO> {
    return transaction {
        Documents.selectAll().map {
            DocumentDTO(
                documentId = it[Documents.documentId],
                recordId = it[Documents.recordId],
                doctorId = it[Documents.doctorId],
                src = it[Documents.src],
                createAt = it[Documents.createAt]
            )
        }
    }
}

fun getDocumentById(documentId: Int): DocumentDTO? {
    return transaction {
        Documents.selectAll().where { Documents.documentId eq documentId }
            .map {
                DocumentDTO(
                    documentId = it[Documents.documentId],
                    recordId = it[Documents.recordId],
                    doctorId = it[Documents.doctorId],
                    src = it[Documents.src],
                    createAt = it[Documents.createAt]
                )
            }
            .singleOrNull()
    }
}

fun getDocumentsByRecordId(recordId: Int): List<DocumentDTO> {
    return transaction {
        Documents.selectAll().where { Documents.recordId eq recordId }
            .map {
                DocumentDTO(
                    documentId = it[Documents.documentId],
                    recordId = it[Documents.recordId],
                    doctorId = it[Documents.doctorId],
                    src = it[Documents.src],
                    createAt = it[Documents.createAt]
                )
            }
    }
}

fun updateDocument(documentId: Int, src: String): Boolean {
    return transaction {
        Documents.update({ Documents.documentId eq documentId }) {
            it[Documents.src] = src
            it[Documents.createAt] = org.joda.time.DateTime.now()
        } > 0
    }
}

fun deleteDocument(documentId: Int): Boolean {
    return transaction {
        Documents.deleteWhere { Documents.documentId eq documentId } > 0
    }
}
