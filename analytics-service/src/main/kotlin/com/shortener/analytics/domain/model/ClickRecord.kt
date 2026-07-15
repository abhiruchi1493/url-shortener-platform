package com.shortener.analytics.domain.model

import java.time.Instant

data class ClickRecord(
    val id: Long?,
    val shortCode: String,
    val originalUrl: String,
    val occurredAt: Instant,
    val userAgent: String?,
    val ipHash: String?,
    val referer: String?
)

data class ShortCodeStats(
    val shortCode: String,
    val totalClicks: Long,
    val lastClickAt: Instant?
)
