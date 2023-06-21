package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.BooleanType

class BooleanLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<Boolean>(rawContent, range) {
    override val value = raw.toBooleanStrict()
    override fun evaluate() =
        Completion.Normal(BooleanType.from(value))
}
