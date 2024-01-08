package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("data property")
data class DataProperty(
    var value: LanguageType,
    var writable: Boolean,
    override var enumerable: Boolean,
    override var configurable: Boolean,
): Property() {
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
    override fun getValue(thisValue: LanguageType) =
        value.toNormal()
    override fun toDescriptorObject(): ObjectType {
        val obj = ObjectType.createNormal().apply {
            createDataProperty("value".languageValue, value)
            createDataProperty("writable".languageValue, writable.languageValue)
        }
        return super.toDescriptorObject(obj)
    }

    companion object {
        private val VALUE_DEFAULT = NullType
        private const val WRITABLE_DEFAULT = true
        fun sealed(value: LanguageType) =
            DataProperty(value, writable=false, enumerable=false, configurable=false)
    }
}
