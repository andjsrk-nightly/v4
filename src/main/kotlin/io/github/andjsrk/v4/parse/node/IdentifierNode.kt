package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.not
import io.github.andjsrk.v4.parse.ReservedWord
import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token
import io.github.andjsrk.v4.util.isOneOf

class IdentifierNode(token: Token): ExpressionNode, ObjectLiteralKeyNode {
    val value = token.rawContent
    override val range = token.range
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)

    val isSpecIdentifier get() =
        value.not { isOneOf(ReservedWord.values().map { it.value }) }
}
