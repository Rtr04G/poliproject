package com.dao

import com.dto.TemplateDTO
import com.example.DBService.Templates
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun createTemplate(src: String): Int {
    return transaction {
        Templates.insert {
            it[Templates.src] = src
            it[Templates.createAt] = org.joda.time.DateTime.now()
        } get Templates.templateId
    }
}

fun getAllTemplates(): List<TemplateDTO> {
    return transaction {
        Templates.selectAll().map {
            TemplateDTO(
                templateId = it[Templates.templateId],
                src = it[Templates.src],
                createAt = it[Templates.createAt]
            )
        }
    }
}

fun getTemplateById(templateId: Int): TemplateDTO? {
    return transaction {
        Templates.selectAll().where { Templates.templateId eq templateId }
            .map {
                TemplateDTO(
                    templateId = it[Templates.templateId],
                    src = it[Templates.src],
                    createAt = it[Templates.createAt]
                )
            }
            .singleOrNull()
    }
}

fun updateTemplate(templateId: Int, src: String): Boolean {
    return transaction {
        Templates.update({ Templates.templateId eq templateId }) {
            it[Templates.src] = src
            it[Templates.createAt] = org.joda.time.DateTime.now()
        } > 0
    }
}

fun deleteTemplate(templateId: Int): Boolean {
    return transaction {
        Templates.deleteWhere { Templates.templateId.eq(templateId) } > 0
    }
}
