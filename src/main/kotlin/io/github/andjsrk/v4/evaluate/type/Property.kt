package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec

@EsSpec("Property Descriptor")
sealed interface Property: AbstractType {
    var enumerable: Boolean
    var configurable: Boolean
    fun clone(): Property

    companion object {
        internal const val ENUMERABLE_DEFAULT = true
        internal const val CONFIGURABLE_DEFAULT = true
    }
}
