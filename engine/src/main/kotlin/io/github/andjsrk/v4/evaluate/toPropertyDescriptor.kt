package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.neverHappens

internal fun ObjectType.toPropertyDescriptor(): MaybeThrow<Property> {
    val hasValueField = hasOwnProperty("value".languageValue)
        .orReturnThrow { return it }
        .value
    val hasWritableField = hasOwnProperty("writable".languageValue)
        .orReturnThrow { return it }
        .value
    val hasGetField = hasOwnProperty("get".languageValue)
        .orReturnThrow { return it }
        .value
    val hasSetField = hasOwnProperty("set".languageValue)
        .orReturnThrow { return it }
        .value

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
                    .orReturnThrow { return it }
                val writable = getOptionalBooleanPropertyValueOrReturn("writable".languageValue) { return it }
                DataProperty(value, writable, enumerable, configurable)
            }
            isAccessor -> {
                val getter = getOwnPropertyValue("get".languageValue)
                    .orReturnThrow { return it }
                    ?.requireToBe<FunctionType> { return it }
                val setter = getOwnPropertyValue("set".languageValue)
                    .orReturnThrow { return it }
                    ?.requireToBe<FunctionType> { return it }
                AccessorProperty(getter, setter, enumerable, configurable)
            }
            else -> neverHappens()
        }
    )
}

private inline fun ObjectType.getOptionalBooleanPropertyValueOrReturn(key: PropertyKey, rtn: ThrowReturnLambda) =
    getOwnPropertyValue(key)
        .orReturnThrow(rtn)
        ?.requireToBe<BooleanType>(rtn)
        ?.value
