package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.parse.node.Node

internal inline fun Node.evaluateOrReturn(`return`: (Completion) -> Nothing) =
    returnIfAbrupt(evaluate(), `return`)
