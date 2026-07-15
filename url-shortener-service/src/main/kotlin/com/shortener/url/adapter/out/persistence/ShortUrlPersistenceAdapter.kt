package com.shortener.url.adapter.out.persistence

import com.shortener.url.domain.model.ShortCode
import com.shortener.url.domain.model.ShortUrl
import com.shortener.url.domain.port.out.ShortUrlRepositoryPort
import org.springframework.stereotype.Component

/**
 * Secondary (driven) adapter. Translates the domain aggregate <-> JPA entity.
 * This is the only place that knows ShortUrl is backed by a relational table.
 */
@Component
class ShortUrlPersistenceAdapter(
    private val jpaRepository: ShortUrlJpaRepository
) : ShortUrlRepositoryPort {

    override fun save(shortUrl: ShortUrl): ShortUrl {
        val existing = shortUrl.id?.let { jpaRepository.findById(it).orElse(null) }
        val entity = (existing ?: ShortUrlJpaEntity()).apply {
            shortCode = shortUrl.shortCode.value
            originalUrl = shortUrl.originalUrl
            createdAt = shortUrl.createdAt
            expiresAt = shortUrl.expiresAt
            clickCount = shortUrl.clickCount
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun findByShortCode(shortCode: ShortCode): ShortUrl? =
        jpaRepository.findByShortCode(shortCode.value).map { it.toDomain() }.orElse(null)

    override fun existsByShortCode(shortCode: ShortCode): Boolean =
        jpaRepository.existsByShortCode(shortCode.value)

    private fun ShortUrlJpaEntity.toDomain(): ShortUrl = ShortUrl.reconstitute(
        id = requireNotNull(id),
        shortCode = ShortCode(shortCode),
        originalUrl = originalUrl,
        createdAt = createdAt,
        expiresAt = expiresAt,
        clickCount = clickCount
    )
}
