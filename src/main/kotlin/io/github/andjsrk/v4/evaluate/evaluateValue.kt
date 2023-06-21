package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.parse.node.Node

fun Node.evaluateValue(): NonEmptyNormalOrAbrupt {
    val res = returnIfAbrupt(evaluate()) { return it }
    return getValue(res)
}
