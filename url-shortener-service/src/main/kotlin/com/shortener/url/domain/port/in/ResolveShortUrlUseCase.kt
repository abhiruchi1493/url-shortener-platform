package com.shortener.url.domain.port.`in`

data class ResolveShortUrlQuery(
    val shortCode: String,
    val userAgent: String?,
    val ipAddress: String?,
    val referer: String?
)

interface ResolveShortUrlUseCase {
    fun resolve(query: ResolveShortUrlQuery): String
}
