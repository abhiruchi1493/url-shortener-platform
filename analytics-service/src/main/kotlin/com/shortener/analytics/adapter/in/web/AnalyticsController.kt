package com.shortener.analytics.adapter.`in`.web

import com.shortener.analytics.domain.port.`in`.GetStatsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AnalyticsController(
    private val getStatsUseCase: GetStatsUseCase
) {
    @GetMapping("/api/v1/stats/{shortCode}")
    fun getStats(@PathVariable shortCode: String) = getStatsUseCase.getStats(shortCode)
}
