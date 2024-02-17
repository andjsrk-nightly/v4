package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.evaluate.type.*

private val iteratorIterator = method(SymbolType.WellKnown.iterator) { thisArg, _ ->
    thisArg.toNormal()
}

val IteratorInstancePrototype = PrototypeObjectType(
    lazy { Object.instancePrototype },
    mutableMapOf(
        sealedMethod(iteratorIterator),
    ),
)
