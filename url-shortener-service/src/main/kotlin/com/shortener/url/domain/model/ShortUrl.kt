package com.shortener.url.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Value object — a short code is always valid by construction.
 */
data class ShortCode(val value: String) {
    init {
        require(value.isNotBlank()) { "Short code must not be blank" }
        require(value.length in 4..12) { "Short code length must be between 4 and 12 characters" }
        require(value.all { it.isLetterOrDigit() }) { "Short code must be alphanumeric" }
    }

    override fun toString(): String = value
}

/**
 * Aggregate root for a shortened URL. Framework-free: no JPA/Spring annotations here.
 * Persistence concerns live entirely in the adapter layer.
 */
class ShortUrl private constructor(
    val id: Long?,
    val shortCode: ShortCode,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val clickCount: Long
) {
    fun isExpired(now: Instant = Instant.now()): Boolean =
        expiresAt != null && now.isAfter(expiresAt)

    fun withIncrementedClicks(): ShortUrl =
        ShortUrl(id, shortCode, originalUrl, createdAt, expiresAt, clickCount + 1)

    companion object {
        fun create(
            shortCode: ShortCode,
            originalUrl: String,
            ttl: Duration?,
            now: Instant = Instant.now()
        ): ShortUrl {
            require(isValidUrl(originalUrl)) { "Invalid URL: $originalUrl" }
            return ShortUrl(
                id = null,
                shortCode = shortCode,
                originalUrl = originalUrl,
                createdAt = now,
                expiresAt = ttl?.let { now.plus(it) },
                clickCount = 0
            )
        }

        /** Used by the persistence adapter to rebuild the aggregate from a stored row. */
        fun reconstitute(
            id: Long,
            shortCode: ShortCode,
            originalUrl: String,
            createdAt: Instant,
            expiresAt: Instant?,
            clickCount: Long
        ) = ShortUrl(id, shortCode, originalUrl, createdAt, expiresAt, clickCount)

        private fun isValidUrl(url: String): Boolean = try {
            val uri = java.net.URI(url)
            uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }
}
