package io.github.andjsrk.v4.parse.node.literal

import io.github.andjsrk.v4.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class StringLiteralNode(token: Token): DynamicPrimitiveLiteralNode<String>() {
    override val raw = token.rawContent
    override val value = token.literal
    override val range = token.range
    override fun DynamicPrimitiveLiteralRaw.toActualValue() =
        throw NotImplementedError("Useless because 'value' has been override")
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
