package com.shortener.url.adapter.out.codegen

import com.shortener.url.domain.model.ShortCode
import com.shortener.url.domain.port.out.ShortCodeGeneratorPort
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class Base62ShortCodeGenerator : ShortCodeGeneratorPort {

    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private val random = SecureRandom()

    override fun generate(length: Int): ShortCode {
        val code = buildString(length) {
            repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
        return ShortCode(code)
    }
}
