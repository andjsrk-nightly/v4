package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.parse.node.StatementListNode

internal fun StatementListNode.evaluateStatements(): NormalOrAbrupt {
    val value = elements
        .map { it.evaluateOrReturn { return it } }
        .foldRight(empty as NormalOrAbrupt) { it, acc -> updateEmpty(acc, it) }
    return value
}
