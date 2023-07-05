package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.missingBranch
import kotlin.reflect.KClass

internal inline fun <reified V: LanguageType> generalizedDescriptionOf(_value: V? = null) =
    generalizedDescriptionOf(V::class)
internal fun generalizedDescriptionOf(clazz: KClass<*>) =
    when {
        clazz.isSubClassOf<NullType>() -> "null"
        clazz.isSubClassOf<StringType>() -> "a string"
        clazz.isSubClassOf<NumberType>() -> "a number"
        clazz.isSubClassOf<BooleanType>() -> "a boolean"
        clazz.isSubClassOf<BigIntType>() -> "a BigInt"
        clazz.isSubClassOf<SymbolType>() -> "a symbol"
        clazz.isSubClassOf<ArrayType>() -> "an array"
        clazz.isSubClassOf<FunctionType>() -> "a function"
        clazz.isSubClassOf<ObjectType>() -> "an object"
        clazz == NumericType::class -> "either a number or a BigInt"
        else -> missingBranch()
    }

private inline fun <reified P> KClass<out Any>.isSubClassOf() =
    this.java.isSubClassOf(P::class.java)
private fun <S, P> Class<S>.isSubClassOf(parent: Class<P>): Boolean =
    when (superclass) {
        null -> false
        parent -> true
        else -> superclass.isSubClassOf(parent)
    }
