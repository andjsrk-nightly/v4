package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.StatementListNode

internal fun evaluateStatements(node: StatementListNode) = lazyFlow f@ {
    node.elements
        .map {
            yieldAll(it.evaluate())
                .orReturn { return@f it }
        }
        .foldRight<_, MaybeEmptyOrAbrupt>(empty) { it, acc -> updateEmpty(acc, it) }
}
