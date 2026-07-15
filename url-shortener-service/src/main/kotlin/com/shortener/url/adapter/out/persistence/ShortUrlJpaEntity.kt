package com.shortener.url.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant

@Entity
@Table(name = "short_urls", indexes = [Index(name = "idx_short_code", columnList = "short_code", unique = true)])
class ShortUrlJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "short_code", nullable = false, unique = true, length = 12)
    var shortCode: String = "",

    @Column(name = "original_url", nullable = false, length = 2048)
    var originalUrl: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,

    @Column(name = "click_count", nullable = false)
    var clickCount: Long = 0,

    @Version
    var version: Long = 0
)
