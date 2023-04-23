package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Range

data class Token(
    val type: TokenType,
    val rawContent: String,
    val literal: String,
    val afterLineTerminator: Boolean,
    private val startPos: Int,
) {
    val range = Range.since(startPos, rawContent.length)
    internal class Builder {
        var startPos = 0
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
