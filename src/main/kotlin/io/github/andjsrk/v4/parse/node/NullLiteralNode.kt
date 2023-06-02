package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.parse.stringifyLikeDataClass
import io.github.andjsrk.v4.tokenize.Token

class NullLiteralNode(token: Token): PrimitiveLiteralNode(token) {
    override fun toString() =
        stringifyLikeDataClass(::range)
    override fun evaluate() =
        Completion.normal(NullType)
}
