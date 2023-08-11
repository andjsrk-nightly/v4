package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NormalOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.StatementListNode

internal fun evaluateStatements(node: StatementListNode) =
    EvalFlow<LanguageType?> {
        `return`(
            node.elements
                .map {
                    it.evaluate()
                        .yieldAllOrEmpty(this)
                        .returnIfAbrupt { println("eval stmt: wtf"); `return`(it) }
                        .also { println("eval stmt: $it") }
                }
                .foldRight<_, NormalOrAbrupt>(empty) { it, acc -> updateEmpty(acc, it) }
        )
    }
