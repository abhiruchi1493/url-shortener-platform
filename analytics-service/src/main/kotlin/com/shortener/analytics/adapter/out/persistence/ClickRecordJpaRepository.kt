package com.shortener.analytics.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ClickRecordJpaRepository : JpaRepository<ClickRecordJpaEntity, Long> {
    fun countByShortCode(shortCode: String): Long

    @Query("SELECT MAX(c.occurredAt) FROM ClickRecordJpaEntity c WHERE c.shortCode = :shortCode")
    fun lastClickAt(shortCode: String): Instant?
}
