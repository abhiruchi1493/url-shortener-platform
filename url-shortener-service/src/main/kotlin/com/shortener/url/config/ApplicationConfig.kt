package com.shortener.url.config

import com.shortener.url.domain.port.out.ClickEventPublisherPort
import com.shortener.url.domain.port.out.ShortCodeGeneratorPort
import com.shortener.url.domain.port.out.ShortUrlRepositoryPort
import com.shortener.url.domain.service.UrlShortenerService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Composition root for the hexagon: wires the framework-free application
 * service to its Spring-managed adapters. Keeps @Service annotations out
 * of the domain/application layer entirely.
 */
@Configuration
class ApplicationConfig {

    @Bean
    fun urlShortenerService(
        repository: ShortUrlRepositoryPort,
        codeGenerator: ShortCodeGeneratorPort,
        publisher: ClickEventPublisherPort,
        @Value("\${app.base-url}") baseUrl: String
    ) = UrlShortenerService(repository, codeGenerator, publisher, baseUrl)
}
