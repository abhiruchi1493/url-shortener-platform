package com.shortener.analytics.domain.port.`in`

import com.shortener.analytics.domain.model.ShortCodeStats

interface GetStatsUseCase {
    fun getStats(shortCode: String): ShortCodeStats
}
