package com.shortener.url.domain.service

import com.shortener.events.ClickEvent
import com.shortener.url.domain.exception.ShortCodeGenerationException
import com.shortener.url.domain.exception.ShortUrlExpiredException
import com.shortener.url.domain.exception.ShortUrlNotFoundException
import com.shortener.url.domain.model.ShortCode
import com.shortener.url.domain.model.ShortUrl
import com.shortener.url.domain.port.`in`.CreateShortUrlCommand
import com.shortener.url.domain.port.`in`.CreateShortUrlUseCase
import com.shortener.url.domain.port.`in`.ResolveShortUrlQuery
import com.shortener.url.domain.port.`in`.ResolveShortUrlUseCase
import com.shortener.url.domain.port.`in`.ShortUrlResult
import com.shortener.url.domain.port.out.ClickEventPublisherPort
import com.shortener.url.domain.port.out.ShortCodeGeneratorPort
import com.shortener.url.domain.port.out.ShortUrlRepositoryPort
import java.security.MessageDigest
import java.time.Instant

private const val MAX_GENERATION_ATTEMPTS = 5

/**
 * Application service — orchestrates the domain model via outbound ports.
 * Deliberately plain Kotlin: no @Service, no Spring imports. Wired into the
 * Spring context via a @Configuration class in the config package, which keeps
 * the hexagon's core free of framework concerns and trivially unit-testable.
 */
class UrlShortenerService(
    private val repository: ShortUrlRepositoryPort,
    private val codeGenerator: ShortCodeGeneratorPort,
    private val clickEventPublisher: ClickEventPublisherPort,
    private val baseUrl: String
) : CreateShortUrlUseCase, ResolveShortUrlUseCase {

    override fun createShortUrl(command: CreateShortUrlCommand): ShortUrlResult {
        val shortCode = command.customAlias
            ?.let { ShortCode(it) }
            ?.also { check(!repository.existsByShortCode(it)) { "Alias already taken: $it" } }
            ?: generateUniqueShortCode()

        val shortUrl = ShortUrl.create(
            shortCode = shortCode,
            originalUrl = command.originalUrl,
            ttl = command.ttl
        )
        val saved = repository.save(shortUrl)
        return ShortUrlResult(
            shortCode = saved.shortCode.value,
            originalUrl = saved.originalUrl,
            shortUrl = "$baseUrl/${saved.shortCode.value}"
        )
    }

    override fun resolve(query: ResolveShortUrlQuery): String {
        val code = ShortCode(query.shortCode)
        val shortUrl = repository.findByShortCode(code)
            ?: throw ShortUrlNotFoundException(query.shortCode)

        if (shortUrl.isExpired()) {
            throw ShortUrlExpiredException(query.shortCode)
        }

        repository.save(shortUrl.withIncrementedClicks())

        clickEventPublisher.publish(
            ClickEvent(
                shortCode = shortUrl.shortCode.value,
                originalUrl = shortUrl.originalUrl,
                occurredAt = Instant.now(),
                userAgent = query.userAgent,
                ipHash = query.ipAddress?.let { hashIp(it) },
                referer = query.referer
            )
        )

        return shortUrl.originalUrl
    }

    private fun generateUniqueShortCode(): ShortCode {
        repeat(MAX_GENERATION_ATTEMPTS) {
            val candidate = codeGenerator.generate()
            if (!repository.existsByShortCode(candidate)) return candidate
        }
        throw ShortCodeGenerationException(
            "Failed to generate a unique short code after $MAX_GENERATION_ATTEMPTS attempts"
        )
    }

    private fun hashIp(ip: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(ip.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
}
