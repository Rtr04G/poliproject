package com.example.poli_project.model

import org.json.JSONArray

data class LPU(
    val lpuId: Int,
    val address: String,
    val phone: String
)

fun parseLPUs(jsonArray: JSONArray): List<LPU> {
    val lpus = mutableListOf<LPU>()
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        lpus.add(
            LPU(
                lpuId = jsonObject.getInt("lpuId"),
                address = jsonObject.getString("address"),
                phone = jsonObject.getString("phone")
            )
        )
    }
    return lpus
}