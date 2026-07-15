package com.shortener.events

import java.time.Instant

/**
 * Integration event published by url-shortener-service and consumed by analytics-service.
 * This is the contract between the two microservices — keep it stable and additive-only.
 */
data class ClickEvent(
    val shortCode: String,
    val originalUrl: String,
    val occurredAt: Instant,
    val userAgent: String?,
    val ipHash: String?,
    val referer: String?
)
