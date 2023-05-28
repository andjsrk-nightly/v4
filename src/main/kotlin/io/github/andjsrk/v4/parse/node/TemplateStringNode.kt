package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class TemplateStringNode(token: Token): Node {
    val raw = token.rawContent
    val value = token.literal
    override val range = token.range
    override fun toString() =
        stringifyLikeDataClass(::raw, ::value, ::range)
}
