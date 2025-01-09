package com.dao

import com.dto.LPUDTO
import com.example.DBService.LPUs
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun createLPUs(address: String, phone: String): Int {
    return transaction {
        LPUs.insert {
            it[LPUs.address] = address
            it[LPUs.phone] = phone
        } get LPUs.lpuId
    }
}

fun getAllLPUs(): List<LPUDTO> {
    return transaction {
        LPUs.selectAll().map {
            LPUDTO(
                lpuId = it[LPUs.lpuId],
                address = it[LPUs.address],
                phone = it[LPUs.phone]
            )
        }
    }
}

fun getLPUsById(lpuId: Int): LPUDTO? {
    return transaction {
        LPUs.selectAll().where { LPUs.lpuId eq lpuId }
            .map {
                LPUDTO(
                    lpuId = it[LPUs.lpuId],
                    address = it[LPUs.address],
                    phone = it[LPUs.phone]
                )
            }
            .singleOrNull()
    }
}

fun updateLPUs(lpuId: Int, address: String, phone: String): Boolean {
    return transaction {
        LPUs.update({ LPUs.lpuId eq lpuId }) {
            it[LPUs.address] = address
            it[LPUs.phone] = phone
        } > 0
    }
}

fun deleteLPUs(lpuId: Int): Boolean {
    return transaction {
        LPUs.deleteWhere { LPUs.lpuId eq lpuId } > 0
    }
}
