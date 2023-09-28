package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeEmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.parse.node.StatementListNode

internal fun evaluateStatements(node: StatementListNode): MaybeEmptyOrAbrupt {
    return node.elements
        .map {
            it.evaluate()
                .orReturn { return it }
        }
        .foldRight<_, MaybeEmptyOrAbrupt>(empty) { it, acc -> updateEmpty(acc, it) }
}
