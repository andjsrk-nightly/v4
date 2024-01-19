package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.toNormal

class BooleanLiteralNode(rawContent: String, range: Range): DynamicPrimitiveLiteralNode<Boolean>(rawContent, range) {
    override val value = raw.toBooleanStrict()
    override fun evaluate() = lazyFlowNoYields {
        value
            .languageValue
            .toNormal()
    }
}
