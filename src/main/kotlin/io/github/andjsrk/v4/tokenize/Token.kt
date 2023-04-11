package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Location

data class Token(
    val type: TokenType,
    val literal: String,
    val afterLineTerminator: Boolean,
    private val startPos: Int,
) {
    val location get() =
        Location.since(startPos, literal.length)
    internal class Builder(private val startPos: Int) {
        lateinit var type: TokenType
        var literal = ""
        var afterLineTerminator = false
        fun build() =
            Token(type, literal, afterLineTerminator, startPos)
    }
}
