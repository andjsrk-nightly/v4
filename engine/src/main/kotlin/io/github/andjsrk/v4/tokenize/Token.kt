package io.github.andjsrk.v4.tokenize

import io.github.andjsrk.v4.Range

data class Token(
    val type: TokenType,
    val rawContent: String,
    val literal: String,
    val isPrevLineTerminator: Boolean,
    private val startPos: Int,
) {
    val range = Range.since(startPos, rawContent.length)
    internal class Builder {
        var startPos = 0
        lateinit var type: TokenType
        var rawContent = ""
        var literal = ""
        var isPrevLineTerminator = false
        fun build(type: TokenType = this.type) =
            Token(type, rawContent, literal, isPrevLineTerminator, startPos)
        fun buildIllegal() =
            build(TokenType.ILLEGAL)
    }
}
