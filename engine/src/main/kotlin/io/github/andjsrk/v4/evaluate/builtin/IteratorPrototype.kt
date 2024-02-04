package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

private val iteratorIterator = method(SymbolType.WellKnown.iterator) { thisArg, args ->
    thisArg.toNormal()
}

val IteratorInstancePrototype = ObjectType(
    lazy { Object.instancePrototype },
    mutableMapOf(
        sealedMethod(iteratorIterator),
    ),
)
