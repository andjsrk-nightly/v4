package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt
import io.github.andjsrk.v4.parse.node.Node

internal fun Node.evaluateValue(): NonEmptyNormalOrAbrupt {
    val res = evaluateOrReturn { return it }
    return getValue(res)
}
