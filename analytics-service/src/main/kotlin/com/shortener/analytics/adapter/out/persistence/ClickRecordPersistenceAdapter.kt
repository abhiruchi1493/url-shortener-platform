package com.shortener.analytics.adapter.out.persistence

import com.shortener.analytics.domain.model.ClickRecord
import com.shortener.analytics.domain.model.ShortCodeStats
import com.shortener.analytics.domain.port.out.ClickRecordRepositoryPort
import org.springframework.stereotype.Component

@Component
class ClickRecordPersistenceAdapter(
    private val jpaRepository: ClickRecordJpaRepository
) : ClickRecordRepositoryPort {

    override fun save(record: ClickRecord): ClickRecord {
        val entity = ClickRecordJpaEntity(
            shortCode = record.shortCode,
            originalUrl = record.originalUrl,
            occurredAt = record.occurredAt,
            userAgent = record.userAgent,
            ipHash = record.ipHash,
            referer = record.referer
        )
        val saved = jpaRepository.save(entity)
        return record.copy(id = saved.id)
    }

    override fun statsFor(shortCode: String): ShortCodeStats = ShortCodeStats(
        shortCode = shortCode,
        totalClicks = jpaRepository.countByShortCode(shortCode),
        lastClickAt = jpaRepository.lastClickAt(shortCode)
    )
}
