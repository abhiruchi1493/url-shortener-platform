package com.shortener.url.domain.service

import com.shortener.url.domain.exception.ShortUrlExpiredException
import com.shortener.url.domain.exception.ShortUrlNotFoundException
import com.shortener.url.domain.model.ShortCode
import com.shortener.url.domain.model.ShortUrl
import com.shortener.url.domain.port.`in`.CreateShortUrlCommand
import com.shortener.url.domain.port.`in`.ResolveShortUrlQuery
import com.shortener.url.domain.port.out.ClickEventPublisherPort
import com.shortener.url.domain.port.out.ShortCodeGeneratorPort
import com.shortener.url.domain.port.out.ShortUrlRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant

/**
 * Pure unit test of the application service — zero Spring context, zero database,
 * zero Kafka. This is the payoff of hexagonal architecture: ports are mocked,
 * the domain rules are exercised directly, tests run in milliseconds.
 */
class UrlShortenerServiceTest {

    private val repository = mockk<ShortUrlRepositoryPort>()
    private val codeGenerator = mockk<ShortCodeGeneratorPort>()
    private val publisher = mockk<ClickEventPublisherPort>(relaxed = true)
    private lateinit var service: UrlShortenerService

    @BeforeEach
    fun setUp() {
        service = UrlShortenerService(repository, codeGenerator, publisher, "http://short.ly")
    }

    @Test
    fun `creates short url with generated code`() {
        val code = ShortCode("abc1234")
        every { codeGenerator.generate(any()) } returns code
        every { repository.existsByShortCode(code) } returns false
        every { repository.save(any()) } answers {
            val arg = firstArg<ShortUrl>()
            ShortUrl.reconstitute(1L, arg.shortCode, arg.originalUrl, arg.createdAt, arg.expiresAt, arg.clickCount)
        }

        val result = service.createShortUrl(CreateShortUrlCommand(originalUrl = "https://example.com"))

        assertEquals("abc1234", result.shortCode)
        assertEquals("http://short.ly/abc1234", result.shortUrl)
    }

    @Test
    fun `rejects taken custom alias`() {
        every { repository.existsByShortCode(ShortCode("taken12")) } returns true

        assertThrows<IllegalStateException> {
            service.createShortUrl(CreateShortUrlCommand(originalUrl = "https://example.com", customAlias = "taken12"))
        }
    }

    @Test
    fun `resolves existing short url and publishes click event`() {
        val shortUrl = ShortUrl.reconstitute(1L, ShortCode("abc1234"), "https://example.com", Instant.now(), null, 0)
        every { repository.findByShortCode(ShortCode("abc1234")) } returns shortUrl
        every { repository.save(any()) } returns shortUrl

        val result = service.resolve(ResolveShortUrlQuery("abc1234", "agent", "127.0.0.1", null))

        assertEquals("https://example.com", result)
        verify(exactly = 1) { publisher.publish(any()) }
    }

    @Test
    fun `throws when short url not found`() {
        every { repository.findByShortCode(ShortCode("missing1")) } returns null

        assertThrows<ShortUrlNotFoundException> {
            service.resolve(ResolveShortUrlQuery("missing1", null, null, null))
        }
    }

    @Test
    fun `throws when short url expired`() {
        val expired = ShortUrl.reconstitute(
            1L, ShortCode("abc1234"), "https://example.com",
            Instant.now().minus(Duration.ofDays(2)), Instant.now().minus(Duration.ofDays(1)), 0
        )
        every { repository.findByShortCode(ShortCode("abc1234")) } returns expired

        assertThrows<ShortUrlExpiredException> {
            service.resolve(ResolveShortUrlQuery("abc1234", null, null, null))
        }
    }
}
