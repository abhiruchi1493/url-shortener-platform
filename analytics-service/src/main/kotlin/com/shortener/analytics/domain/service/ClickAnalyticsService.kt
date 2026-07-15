package com.shortener.analytics.domain.service

import com.shortener.analytics.domain.model.ClickRecord
import com.shortener.analytics.domain.model.ShortCodeStats
import com.shortener.analytics.domain.port.`in`.GetStatsUseCase
import com.shortener.analytics.domain.port.`in`.RecordClickUseCase
import com.shortener.analytics.domain.port.out.ClickRecordRepositoryPort
import com.shortener.events.ClickEvent

class ClickAnalyticsService(
    private val repository: ClickRecordRepositoryPort
) : RecordClickUseCase, GetStatsUseCase {

    override fun recordClick(event: ClickEvent) {
        repository.save(
            ClickRecord(
                id = null,
                shortCode = event.shortCode,
                originalUrl = event.originalUrl,
                occurredAt = event.occurredAt,
                userAgent = event.userAgent,
                ipHash = event.ipHash,
                referer = event.referer
            )
        )
    }

    override fun getStats(shortCode: String): ShortCodeStats = repository.statsFor(shortCode)
}
