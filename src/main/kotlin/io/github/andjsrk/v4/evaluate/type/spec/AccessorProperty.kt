package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("accessor property")
data class AccessorProperty(
    var get: ObjectType? = null,
    var set: ObjectType? = null,
    override var enumerable: Boolean = true,
    override var configurable: Boolean = true,
): Property
