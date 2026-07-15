package com.shortener.analytics.config

import com.shortener.analytics.domain.port.out.ClickRecordRepositoryPort
import com.shortener.analytics.domain.service.ClickAnalyticsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    @Bean
    fun clickAnalyticsService(repository: ClickRecordRepositoryPort) = ClickAnalyticsService(repository)
}
