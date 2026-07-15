package com.shortener.url.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ShortUrlJpaRepository : JpaRepository<ShortUrlJpaEntity, Long> {
    fun findByShortCode(shortCode: String): Optional<ShortUrlJpaEntity>
    fun existsByShortCode(shortCode: String): Boolean
}
