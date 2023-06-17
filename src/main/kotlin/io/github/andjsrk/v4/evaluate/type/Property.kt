package io.github.andjsrk.v4.evaluate.type

sealed interface Property {
    var enumerable: Boolean
    var configurable: Boolean
    fun clone(): Property
}
