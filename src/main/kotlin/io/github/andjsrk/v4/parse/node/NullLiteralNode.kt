package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class NullLiteralNode(range: Range): PrimitiveLiteralNode("null", range) {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() =
        Completion.`null`
}
