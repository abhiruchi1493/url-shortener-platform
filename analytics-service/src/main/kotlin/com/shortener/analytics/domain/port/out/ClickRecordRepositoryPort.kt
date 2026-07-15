package com.shortener.analytics.domain.port.out

import com.shortener.analytics.domain.model.ClickRecord
import com.shortener.analytics.domain.model.ShortCodeStats

interface ClickRecordRepositoryPort {
    fun save(record: ClickRecord): ClickRecord
    fun statsFor(shortCode: String): ShortCodeStats
}
