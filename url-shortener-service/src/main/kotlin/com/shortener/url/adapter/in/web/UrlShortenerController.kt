package com.shortener.url.adapter.`in`.web

import com.shortener.url.adapter.`in`.web.dto.CreateShortUrlRequest
import com.shortener.url.adapter.`in`.web.dto.CreateShortUrlResponse
import com.shortener.url.domain.port.`in`.CreateShortUrlCommand
import com.shortener.url.domain.port.`in`.CreateShortUrlUseCase
import com.shortener.url.domain.port.`in`.ResolveShortUrlQuery
import com.shortener.url.domain.port.`in`.ResolveShortUrlUseCase
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.Duration

/**
 * Primary (driving) adapter. Translates HTTP <-> use case commands/queries.
 * Contains no business logic — it only adapts.
 */
@RestController
class UrlShortenerController(
    private val createShortUrlUseCase: CreateShortUrlUseCase,
    private val resolveShortUrlUseCase: ResolveShortUrlUseCase
) {

    @PostMapping("/api/v1/short-urls")
    fun create(@Valid @RequestBody request: CreateShortUrlRequest): ResponseEntity<CreateShortUrlResponse> {
        val result = createShortUrlUseCase.createShortUrl(
            CreateShortUrlCommand(
                originalUrl = request.originalUrl,
                customAlias = request.customAlias,
                ttl = request.ttlSeconds?.let { Duration.ofSeconds(it) }
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateShortUrlResponse(result.shortCode, result.originalUrl, result.shortUrl)
        )
    }

    @GetMapping("/{shortCode}")
    fun redirect(@PathVariable shortCode: String, request: HttpServletRequest): ResponseEntity<Void> {
        val originalUrl = resolveShortUrlUseCase.resolve(
            ResolveShortUrlQuery(
                shortCode = shortCode,
                userAgent = request.getHeader("User-Agent"),
                ipAddress = request.remoteAddr,
                referer = request.getHeader("Referer")
            )
        )
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(originalUrl))
            .build()
    }
}
