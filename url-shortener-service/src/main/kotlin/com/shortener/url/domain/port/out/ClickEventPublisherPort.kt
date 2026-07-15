package com.shortener.url.domain.port.out

import com.shortener.events.ClickEvent

/** Outbound port for telling the outside world (analytics-service) about a click. */
interface ClickEventPublisherPort {
    fun publish(event: ClickEvent)
}
