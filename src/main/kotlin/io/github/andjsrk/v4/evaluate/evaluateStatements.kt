package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.node.StatementListNode

internal fun StatementListNode.evaluateStatements(): Completion {
    val value = elements
        .map { returnIfAbrupt(it.evaluate()) { return it } }
        .foldRight(Completion.empty) { it, acc -> updateEmpty(acc, it) }
    return value
}
