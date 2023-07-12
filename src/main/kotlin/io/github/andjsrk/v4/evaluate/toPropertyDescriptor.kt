package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.neverHappens

internal fun ObjectType.toPropertyDescriptor(): MaybeAbrupt<Property> {
    val hasValueField = hasOwnProperty("value".languageValue)
    val hasWritableField = hasOwnProperty("writable".languageValue)
    val hasGetField = hasOwnProperty("get".languageValue)
    val hasSetField = hasOwnProperty("set".languageValue)

    val isData = hasValueField || hasWritableField
    val isAccessor = hasGetField || hasSetField

    // cannot determine which descriptor needs to be created
    if (isData == isAccessor) return throwError(TypeErrorKind.AMBIGUOUS_PROPERTY_DESCRIPTOR)

    val enumerable = getOptionalBooleanPropertyValueOrReturn("enumerable".languageValue) { return it }
    val configurable = getOptionalBooleanPropertyValueOrReturn("configurable".languageValue) { return it }

    return Completion.WideNormal(
        when {
            isData -> {
                val value = getOwnPropertyValue("value".languageValue)
                    .returnIfAbrupt { return it }
                val writable = getOptionalBooleanPropertyValueOrReturn("writable".languageValue) { return it }
                DataProperty(value, writable, enumerable, configurable)
            }
            isAccessor -> {
                val getter = getOwnPropertyValue("get".languageValue)
                    .returnIfAbrupt { return it }
                    ?.requireToBe<FunctionType> { return it }
                val setter = getOwnPropertyValue("set".languageValue)
                    .returnIfAbrupt { return it }
                    ?.requireToBe<FunctionType> { return it }
                AccessorProperty(getter, setter, enumerable, configurable)
            }
            else -> neverHappens()
        }
    )
}

private inline fun ObjectType.getOptionalBooleanPropertyValueOrReturn(key: PropertyKey, `return`: AbruptReturnLambda) =
    getOwnPropertyValue(key)
        .returnIfAbrupt (`return`)
        ?.requireToBe<BooleanType>(`return`)
        ?.value
