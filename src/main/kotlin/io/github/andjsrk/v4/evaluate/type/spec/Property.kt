package io.github.andjsrk.v4.evaluate.type.spec

sealed interface Property {
    var enumerable: Boolean
    var configurable: Boolean
}
