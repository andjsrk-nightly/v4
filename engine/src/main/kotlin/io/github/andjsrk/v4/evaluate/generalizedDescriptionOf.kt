package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch
import kotlin.reflect.KClass

internal inline fun <reified V> generalizedDescriptionOf() =
    generalizedDescriptionOf(V::class)
internal inline fun generalizedDescriptionOf(value: LanguageType?) =
    generalizedDescriptionOf((value ?: NullType)::class)
internal fun generalizedDescriptionOf(clazz: KClass<*>) =
    when {
        clazz.`is`<NullType>() -> "null"
        clazz.`is`<StringType>() -> "a string"
        clazz.`is`<NumberType>() -> "a number"
        clazz.`is`<BigIntType>() -> "a BigInt"
        clazz.`is`<NumericType<*>>() -> "a number or a BigInt"
        clazz.`is`<BooleanType>() -> "a boolean"
        clazz.`is`<SymbolType>() -> "a symbol"
        clazz.`is`<LanguageTypePropertyKey>() -> "a property key"
        clazz.`is`<ArrayType>() -> "an array"
        clazz.`is`<FunctionType>() -> "a function"
        clazz.`is`<RegExpType>() -> "a regular expression"
        clazz.`is`<ObjectType>() -> "an object"
        else -> missingBranch()
    }

private inline fun <reified P> KClass<out Any>.`is`() =
    this.java.`is`(P::class.java)
private fun <S, P> Class<S>.`is`(other: Class<P>): Boolean =
    this == other || when (superclass) {
        null -> false
        other -> true
        else -> superclass.`is`(other)
    }
