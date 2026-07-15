package com.shortener.analytics.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "click_records", indexes = [Index(name = "idx_click_short_code", columnList = "short_code")])
class ClickRecordJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "short_code", nullable = false, length = 12)
    var shortCode: String = "",

    @Column(name = "original_url", nullable = false, length = 2048)
    var originalUrl: String = "",

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now(),

    @Column(name = "user_agent")
    var userAgent: String? = null,

    @Column(name = "ip_hash", length = 16)
    var ipHash: String? = null,

    @Column(name = "referer")
    var referer: String? = null
)
