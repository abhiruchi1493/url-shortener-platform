package com.shortener.analytics.domain.service

import com.shortener.analytics.domain.model.ClickRecord
import com.shortener.analytics.domain.model.ShortCodeStats
import com.shortener.analytics.domain.port.out.ClickRecordRepositoryPort
import com.shortener.events.ClickEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ClickAnalyticsServiceTest {

    private val repository = mockk<ClickRecordRepositoryPort>()
    private val service = ClickAnalyticsService(repository)

    @Test
    fun `records click event as a click record`() {
        val captured = slot<ClickRecord>()
        every { repository.save(capture(captured)) } answers { firstArg() }

        val event = ClickEvent(
            shortCode = "abc1234",
            originalUrl = "https://example.com",
            occurredAt = Instant.now(),
            userAgent = "agent",
            ipHash = "hash",
            referer = "https://referer.com"
        )

        service.recordClick(event)

        verify(exactly = 1) { repository.save(any()) }
        assertEquals("abc1234", captured.captured.shortCode)
    }

    @Test
    fun `returns stats for a short code`() {
        every { repository.statsFor("abc1234") } returns ShortCodeStats("abc1234", 42, Instant.now())

        val stats = service.getStats("abc1234")

        assertEquals(42, stats.totalClicks)
    }
}
