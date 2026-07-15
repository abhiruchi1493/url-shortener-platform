package com.shortener.url.domain.exception

class ShortUrlNotFoundException(shortCode: String) : RuntimeException("Short URL not found: $shortCode")
class ShortUrlExpiredException(shortCode: String) : RuntimeException("Short URL has expired: $shortCode")
class ShortCodeGenerationException(message: String) : RuntimeException(message)
