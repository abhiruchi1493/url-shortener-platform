package com.shortener.url.adapter.`in`.web

import com.shortener.url.adapter.`in`.web.dto.ErrorResponse
import com.shortener.url.domain.exception.ShortUrlExpiredException
import com.shortener.url.domain.exception.ShortUrlNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException::class)
    fun handleNotFound(ex: ShortUrlNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message ?: "Not found", 404))

    @ExceptionHandler(ShortUrlExpiredException::class)
    fun handleExpired(ex: ShortUrlExpiredException) =
        ResponseEntity.status(HttpStatus.GONE).body(ErrorResponse(ex.message ?: "Expired", 410))

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequest(ex: RuntimeException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message ?: "Bad request", 400))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(message, 400))
    }
}
