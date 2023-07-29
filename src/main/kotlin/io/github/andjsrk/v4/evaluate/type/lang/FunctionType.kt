package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Function
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.NonEmptyNormalOrAbrupt

sealed class FunctionType(
    val name: PropertyKey?,
    requiredParameterCount: UInt,
    val env: DeclarativeEnvironment,
): ObjectType(
    lazy { Function.instancePrototype },
    mutableMapOf(
        "requiredParameterCount".sealedData(requiredParameterCount.toDouble().languageValue),
    ),
) {
    val realm = runningExecutionContext.realm
    abstract val isArrow: Boolean
    @EsSpec("[[Call]]")
    abstract fun _call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt
    // TODO: throw an error if thisArg is not provided but the function depends on it
}

internal inline fun <reified R: LanguageType> FunctionType.callAndRequireToBe(thisArg: LanguageType?, args: List<LanguageType>, `return`: AbruptReturnLambda) =
    _call(thisArg, args)
        .returnIfAbrupt(`return`)
        .requireToBe<R>(`return`)

internal inline fun FunctionType.callCollectionCallback(
    element: LanguageType,
    index: Int,
    collection: LanguageType,
    `return`: AbruptReturnLambda,
) =
    _call(null, listOf(element, index.languageValue, collection))
        .returnIfAbrupt(`return`)

internal inline fun FunctionType.callPredicate(
    element: LanguageType,
    index: Int,
    array: ArrayType,
    `return`: AbruptReturnLambda,
) =
    callCollectionCallback(element, index, array, `return`)
        .requireToBe<BooleanType>(`return`)
        .value
