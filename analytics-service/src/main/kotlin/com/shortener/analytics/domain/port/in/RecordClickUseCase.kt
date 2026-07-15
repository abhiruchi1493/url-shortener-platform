package com.shortener.analytics.domain.port.`in`

import com.shortener.events.ClickEvent

interface RecordClickUseCase {
    fun recordClick(event: ClickEvent)
}
