package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Function
import io.github.andjsrk.v4.evaluate.builtin.sealedData

sealed class FunctionType(
    var name: PropertyKey?,
    requiredParameterCount: UInt,
    open val env: DeclarativeEnvironment?,
    val privateEnv: PrivateEnvironment? = null,
    lazyPrototype: Lazy<PrototypeObjectType> = lazy { Function.instancePrototype },
): ObjectType by ObjectType.Impl(
    lazyPrototype,
    mutableMapOf(
        "requiredParameterCount".sealedData(requiredParameterCount.toDouble().languageValue),
    ),
) {
    var homeObject: ObjectType? = null
    val realm = runningExecutionContext.realm
    val module = getActiveModule()
    abstract val isMethod: Boolean
    /**
     * Note that the method does not add the context to [executionContextStack] automatically.
     */
    @EsSpec("PrepareForOrdinaryCall")
    fun createContextForCall() =
        ExecutionContext(realm, FunctionEnvironment.from(this), this, module = module)
    @EsSpec("Call") // the method implements Call rather than [[Call]] since additional type check is no needed
    abstract fun call(thisArg: LanguageType? = null, args: List<LanguageType> = emptyList()): NonEmptyOrThrow
    abstract fun evaluateBody(thisArg: LanguageType?, args: List<LanguageType>): Completion.FromFunctionBody<*>
    fun callWithSingleArg(value: LanguageType) =
        call(null, listOf(value))
}

internal inline fun FunctionType.callCollectionCallback(
    element: LanguageType,
    index: Int,
    collection: LanguageType,
    rtn: ThrowReturnLambda,
) =
    call(null, listOf(element, index.languageValue, collection))
        .orReturnThrow(rtn)

internal inline fun FunctionType.callCollectionPredicate(
    element: LanguageType,
    index: Int,
    array: ArrayType,
    rtn: ThrowReturnLambda,
) =
    callCollectionCallback(element, index, array, rtn)
        .requireToBe<BooleanType>(rtn)
        .nativeValue
