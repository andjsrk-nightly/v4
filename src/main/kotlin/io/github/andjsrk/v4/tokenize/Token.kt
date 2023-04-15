package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Location

data class Token(
    val type: TokenType,
    val rawContent: String,
    val literal: String,
    val afterLineTerminator: Boolean,
    private val startPos: Int,
) {
    val location get() =
        Location.since(startPos, rawContent.length)
    internal class Builder(private val startPos: Int) {
        lateinit var type: TokenType
        var rawContent = ""
        var literal = ""
        var afterLineTerminator = false
        fun build(type: TokenType = this.type) =
            Token(type, rawContent, literal, afterLineTerminator, startPos)
        fun buildIllegal() =
            build(TokenType.ILLEGAL)
    }
}
