package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Property.Companion.CONFIGURABLE_DEFAULT
import io.github.andjsrk.v4.evaluate.type.Property.Companion.ENUMERABLE_DEFAULT
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType

@EsSpec("data property")
data class DataProperty(
    var value: LanguageType,
    var writable: Boolean,
    override var enumerable: Boolean,
    override var configurable: Boolean,
): Property {
    constructor(
        value: LanguageType? = VALUE_DEFAULT,
        writable: Boolean? = WRITABLE_DEFAULT,
        enumerable: Boolean? = ENUMERABLE_DEFAULT,
        configurable: Boolean? = CONFIGURABLE_DEFAULT,
    ): this(
        value ?: VALUE_DEFAULT,
        writable ?: WRITABLE_DEFAULT,
        enumerable ?: ENUMERABLE_DEFAULT,
        configurable ?: CONFIGURABLE_DEFAULT,
    )
    override fun clone() = copy()

    companion object {
        private val VALUE_DEFAULT = NullType
        private const val WRITABLE_DEFAULT = true
        fun sealed(value: LanguageType) =
            DataProperty(value, writable=false, enumerable=false, configurable=false)
    }
}
