package com.shortener.url.adapter.`in`.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateShortUrlRequest(
    @field:NotBlank(message = "originalUrl must not be blank")
    @field:Pattern(regexp = "^https?://.+", message = "originalUrl must start with http:// or https://")
    val originalUrl: String,
    val customAlias: String? = null,
    val ttlSeconds: Long? = null
)

data class CreateShortUrlResponse(
    val shortCode: String,
    val originalUrl: String,
    val shortUrl: String
)

data class ErrorResponse(
    val message: String,
    val status: Int
)
