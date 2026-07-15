package com.shortener.url.domain.port.out

import com.shortener.url.domain.model.ShortCode

interface ShortCodeGeneratorPort {
    fun generate(length: Int = 7): ShortCode
}
