package com.shortener.url.domain.port.out

import com.shortener.url.domain.model.ShortCode
import com.shortener.url.domain.model.ShortUrl

/** Outbound port — implemented by the persistence adapter. The domain knows nothing about JPA/SQL. */
interface ShortUrlRepositoryPort {
    fun save(shortUrl: ShortUrl): ShortUrl
    fun findByShortCode(shortCode: ShortCode): ShortUrl?
    fun existsByShortCode(shortCode: ShortCode): Boolean
}
