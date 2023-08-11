package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.Node

internal fun Node.evaluateValue() =
    EvalFlow {
        val res = returnIfAbrupt(evaluate()) { return@EvalFlow }
        `return`(getValue(res))
    }
