package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun ObjectLiteralKeyNode.toPropertyKey() = lazyFlow f@ {
    when (this@toPropertyKey) {
        is IdentifierNode -> stringValue
        is ComputedPropertyKeyNode -> yieldAll(expression.evaluateValue())
            .orReturn { return@f it }
            .requireToBePropertyKey { return@f it }
        is StringLiteralNode -> value.languageValue
        is NumberLiteralNode -> value.languageValue.toString(10)
    }
        .toNormal()
}
