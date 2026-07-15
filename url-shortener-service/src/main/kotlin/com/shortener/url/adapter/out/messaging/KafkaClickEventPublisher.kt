package com.shortener.url.adapter.out.messaging

import com.shortener.events.ClickEvent
import com.shortener.url.domain.port.out.ClickEventPublisherPort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaClickEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ClickEvent>
) : ClickEventPublisherPort {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val topic = "url.click-events"

    override fun publish(event: ClickEvent) {
        kafkaTemplate.send(topic, event.shortCode, event)
            .whenComplete { _, ex ->
                if (ex != null) {
                    // Redirect already succeeded for the user; failing to record analytics
                    // must never break the redirect itself, so we only log here.
                    logger.error("Failed to publish click event for ${event.shortCode}", ex)
                }
            }
    }
}
