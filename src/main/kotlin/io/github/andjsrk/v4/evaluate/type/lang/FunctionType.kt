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
    lazyPrototype: Lazy<PrototypeObjectType> = lazy { Function.instancePrototype },
): ObjectType(
    lazyPrototype,
    mutableMapOf(
        "requiredParameterCount".sealedData(requiredParameterCount.toDouble().languageValue),
    ),
) {
    val realm = runningExecutionContext.realm
    abstract val isArrow: Boolean
    @EsSpec("Call") // the method implements Call rather than [[Call]] since additional type check is no needed
    abstract fun call(thisArg: LanguageType?, args: List<LanguageType>): NonEmptyNormalOrAbrupt
}

internal inline fun <reified R: LanguageType> FunctionType.callAndRequireToBe(thisArg: LanguageType?, args: List<LanguageType>, rtn: AbruptReturnLambda) =
    call(thisArg, args)
        .orReturn(rtn)
        .requireToBe<R>(rtn)

internal inline fun FunctionType.callCollectionCallback(
    element: LanguageType,
    index: Int,
    collection: LanguageType,
    rtn: AbruptReturnLambda,
) =
    call(null, listOf(element, index.languageValue, collection))
        .orReturn(rtn)

internal inline fun FunctionType.callPredicate(
    element: LanguageType,
    index: Int,
    array: ArrayType,
    rtn: AbruptReturnLambda,
) =
    callCollectionCallback(element, index, array, rtn)
        .requireToBe<BooleanType>(rtn)
        .value
