package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun ObjectLiteralKeyNode.toPropertyKey(): MaybeAbrupt<PropertyKey> {
    return when (this) {
        is IdentifierNode -> stringValue
        is ComputedPropertyKeyNode -> expression.evaluateValue()
            .orReturn { return it }
            .requireToBePropertyKey { return it }
        is StringLiteralNode -> value.languageValue
        is NumberLiteralNode -> value.languageValue.toString(10)
    }
        .toNormal()
}
