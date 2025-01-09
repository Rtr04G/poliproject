package com.dto

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsDTO(
    val statId: Int,
    val metric: String,
    val value: Float
)
