package com.shortener.analytics.adapter.`in`.messaging

import com.shortener.analytics.domain.port.`in`.RecordClickUseCase
import com.shortener.events.ClickEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/** Primary adapter — consumes the integration event published by url-shortener-service. */
@Component
class ClickEventKafkaListener(
    private val recordClickUseCase: RecordClickUseCase
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["url.click-events"], groupId = "analytics-service")
    fun onClickEvent(event: ClickEvent) {
        runCatching { recordClickUseCase.recordClick(event) }
            .onFailure { logger.error("Failed to process click event for ${event.shortCode}", it) }
    }
}
