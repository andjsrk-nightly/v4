package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch
import kotlin.reflect.KClass

internal inline fun <reified V: LanguageType> generalizedDescriptionOf(_value: V? = null) =
    generalizedDescriptionOf(V::class)
internal fun generalizedDescriptionOf(clazz: KClass<*>) =
    when {
        clazz.`is`<NullType>() -> "null"
        clazz.`is`<StringType>() -> "a string"
        clazz.`is`<NumberType>() -> "a number"
        clazz.`is`<BooleanType>() -> "a boolean"
        clazz.`is`<BigIntType>() -> "a BigInt"
        clazz.`is`<SymbolType>() -> "a symbol"
        clazz.`is`<ArrayType>() -> "an array"
        clazz.`is`<FunctionType>() -> "a function"
        clazz.`is`<ObjectType>() -> "an object"
        clazz == NumericType::class -> "either a number or a BigInt"
        else -> {
            println(clazz)
            missingBranch()
        }
    }

private inline fun <reified P> KClass<out Any>.`is`() =
    this.java.`is`(P::class.java)
private fun <S, P> Class<S>.`is`(other: Class<P>): Boolean =
    this == other || when (superclass) {
        null -> false
        other -> true
        else -> superclass.`is`(other)
    }
