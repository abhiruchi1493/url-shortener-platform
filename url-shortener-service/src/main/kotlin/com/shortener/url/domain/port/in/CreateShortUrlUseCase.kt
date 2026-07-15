package com.shortener.url.domain.port.`in`

import java.time.Duration

data class CreateShortUrlCommand(
    val originalUrl: String,
    val customAlias: String? = null,
    val ttl: Duration? = null
)

data class ShortUrlResult(
    val shortCode: String,
    val originalUrl: String,
    val shortUrl: String
)

/** Inbound port — implemented by the application service, called by the web adapter. */
interface CreateShortUrlUseCase {
    fun createShortUrl(command: CreateShortUrlCommand): ShortUrlResult
}
