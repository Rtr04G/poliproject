package com.dao

import com.dto.AnalyticsDTO
import com.example.DBService.Analytics
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun createAnalytics(metric: String, value: Float): Int {
    return transaction {
        Analytics.insert {
            it[Analytics.metric] = metric
            it[Analytics.value] = value
        } get Analytics.statId
    }
}

fun getAllAnalytics(): List<AnalyticsDTO> {
    return transaction {
        Analytics.selectAll().map {
            AnalyticsDTO(
                statId = it[Analytics.statId],
                metric = it[Analytics.metric],
                value = it[Analytics.value]
            )
        }
    }
}

fun getAnalyticsById(statId: Int): AnalyticsDTO? {
    return transaction {
        Analytics.selectAll().where { Analytics.statId eq statId }
            .map {
                AnalyticsDTO(
                    statId = it[Analytics.statId],
                    metric = it[Analytics.metric],
                    value = it[Analytics.value]
                )
            }
            .singleOrNull()
    }
}

fun updateAnalytics(statId: Int, metric: String, value: Float): Boolean {
    return transaction {
        Analytics.update({ Analytics.statId eq statId }) {
            it[Analytics.metric] = metric
            it[Analytics.value] = value
        } > 0
    }
}

fun deleteAnalytics(statId: Int): Boolean {
    return transaction {
        Analytics.deleteWhere { Analytics.statId eq statId } > 0
    }
}
