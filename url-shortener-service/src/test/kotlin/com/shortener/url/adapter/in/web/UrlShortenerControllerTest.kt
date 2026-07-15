package com.shortener.url.adapter.`in`.web

import com.shortener.url.domain.exception.ShortUrlExpiredException
import com.shortener.url.domain.exception.ShortUrlNotFoundException
import com.shortener.url.domain.port.`in`.CreateShortUrlCommand
import com.shortener.url.domain.port.`in`.CreateShortUrlUseCase
import com.shortener.url.domain.port.`in`.ResolveShortUrlQuery
import com.shortener.url.domain.port.`in`.ResolveShortUrlUseCase
import com.shortener.url.domain.port.`in`.ShortUrlResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(UrlShortenerController::class)
@Import(UrlShortenerControllerTest.TestBeans::class)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var createShortUrlUseCase: StubCreateShortUrlUseCase

    @Autowired
    private lateinit var resolveShortUrlUseCase: StubResolveShortUrlUseCase

    @BeforeEach
    fun setUp() {
        createShortUrlUseCase.reset()
        resolveShortUrlUseCase.reset()
    }

    @Test
    fun `creates short url successfully`() {
        createShortUrlUseCase.handler = { command ->
            require(command.originalUrl == "https://example.com")
            require(command.customAlias == "example")
            require(command.ttl?.seconds == 3600L)
            ShortUrlResult(
                shortCode = "example",
                originalUrl = command.originalUrl,
                shortUrl = "http://short.ly/example"
            )
        }

        mockMvc.post("/api/v1/short-urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "originalUrl": "https://example.com",
                  "customAlias": "example",
                  "ttlSeconds": 3600
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.shortCode") { value("example") }
            jsonPath("$.originalUrl") { value("https://example.com") }
            jsonPath("$.shortUrl") { value("http://short.ly/example") }
        }
    }

    @Test
    fun `redirects resolved short url`() {
        resolveShortUrlUseCase.handler = { query ->
            require(query.shortCode == "abc123")
            require(query.userAgent == "JUnit")
            require(query.referer == "https://referrer.example")
            "https://example.com/article"
        }

        mockMvc.get("/abc123") {
            header("User-Agent", "JUnit")
            header("Referer", "https://referrer.example")
        }.andExpect {
            status { isFound() }
            header { string("Location", "https://example.com/article") }
        }
    }

    @Test
    fun `returns not found when short code does not exist`() {
        resolveShortUrlUseCase.handler = { throw ShortUrlNotFoundException("missing") }

        mockMvc.get("/missing")
            .andExpect {
                status { isNotFound() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("Short URL not found: missing") }
            }
    }

    @Test
    fun `returns gone when short code is expired`() {
        resolveShortUrlUseCase.handler = { throw ShortUrlExpiredException("expired") }

        mockMvc.get("/expired")
            .andExpect {
                status { isGone() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value(410) }
                jsonPath("$.message") { value("Short URL has expired: expired") }
            }
    }

    @Test
    fun `returns bad request for illegal argument from create use case`() {
        createShortUrlUseCase.handler = { throw IllegalArgumentException("Invalid custom alias") }

        mockMvc.post("/api/v1/short-urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"originalUrl": "https://example.com"}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Invalid custom alias") }
        }
    }

    @Test
    fun `returns bad request for illegal state from create use case`() {
        createShortUrlUseCase.handler = { throw IllegalStateException("Custom alias already exists") }

        mockMvc.post("/api/v1/short-urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"originalUrl": "https://example.com", "customAlias": "taken"}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Custom alias already exists") }
        }
    }

    @Test
    fun `returns validation error when original url is blank`() {
        mockMvc.post("/api/v1/short-urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"originalUrl": ""}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("originalUrl: originalUrl must not be blank") }
        }
    }

    @Test
    fun `returns validation error when original url is not http or https`() {
        mockMvc.post("/api/v1/short-urls") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"originalUrl": "ftp://example.com"}"""
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("originalUrl: originalUrl must start with http:// or https://") }
        }
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun createShortUrlUseCase() = StubCreateShortUrlUseCase()

        @Bean
        fun resolveShortUrlUseCase() = StubResolveShortUrlUseCase()
    }
}

class StubCreateShortUrlUseCase : CreateShortUrlUseCase {
    var handler: (CreateShortUrlCommand) -> ShortUrlResult = { command ->
        ShortUrlResult(
            shortCode = "abc123",
            originalUrl = command.originalUrl,
            shortUrl = "http://short.ly/abc123"
        )
    }

    override fun createShortUrl(command: CreateShortUrlCommand): ShortUrlResult = handler(command)

    fun reset() {
        handler = { command ->
            ShortUrlResult(
                shortCode = "abc123",
                originalUrl = command.originalUrl,
                shortUrl = "http://short.ly/abc123"
            )
        }
    }
}

class StubResolveShortUrlUseCase : ResolveShortUrlUseCase {
    var handler: (ResolveShortUrlQuery) -> String = { "https://example.com" }

    override fun resolve(query: ResolveShortUrlQuery): String = handler(query)

    fun reset() {
        handler = { "https://example.com" }
    }
}
