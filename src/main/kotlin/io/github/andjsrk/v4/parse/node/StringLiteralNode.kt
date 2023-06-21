package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.StringType

class StringLiteralNode(rawContent: String, content: String, range: Range): DynamicPrimitiveLiteralNode<String>(rawContent, range), ObjectLiteralKeyNode {
    @EsSpec("SV")
    override val value = content
    override fun evaluate() =
        Completion.Normal(StringType(value))
}
