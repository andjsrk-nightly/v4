package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.node.Node

fun Node.evaluateValue(): Completion {
    val res = returnIfAbrupt(evaluate()) { return it }
    return getValue(res)
}
