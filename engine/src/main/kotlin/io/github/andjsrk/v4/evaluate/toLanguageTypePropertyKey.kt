package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.toWideNormal
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun ObjectLiteralKeyNode.toLanguageTypePropertyKey() = lazyFlow f@ {
    when (this@toLanguageTypePropertyKey) {
        is IdentifierNode -> stringValue
        is ComputedPropertyKeyNode ->
            yieldAll(expression.evaluateValue())
                .orReturn { return@f it }
                .requireToBeLanguageTypePropertyKey { return@f it }
        is StringLiteralNode -> value.languageValue
        is NumberLiteralNode -> value.languageValue.toString(10)
    }
        .toWideNormal()
}
