package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue

@EsSpec("Property Descriptor")
sealed class Property: AbstractType {
    abstract var enumerable: Boolean
    abstract var configurable: Boolean
    abstract fun clone(): Property
    /**
     * Note that the function requires [key] for better error messages.
     */
    abstract fun getValue(thisValue: LanguageType, key: PropertyKey): NonEmptyOrThrow
    /**
     * Note that the function requires [key] for better error messages.
     */
    abstract fun setValue(thisValue: LanguageType, key: PropertyKey, value: LanguageType): EmptyOrThrow
    abstract fun toDescriptorObject(): ObjectType
    protected fun toDescriptorObject(obj: ObjectType): ObjectType {
        return obj.apply {
            createDataProperty("enumerable".languageValue, enumerable.languageValue)
            createDataProperty("configurable".languageValue, configurable.languageValue)
        }
    }

    companion object {
        internal const val ENUMERABLE_DEFAULT = true
        internal const val CONFIGURABLE_DEFAULT = true
    }
}
