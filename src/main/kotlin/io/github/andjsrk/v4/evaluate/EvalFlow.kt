package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsGenerator
import io.github.andjsrk.v4.error.ErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.SyncGeneratorType
import io.github.andjsrk.v4.neverHappens
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted

/**
 * Indicates a lambda that stops flow, which will be used on inline functions.
 */
internal typealias ReturnLambda = () -> Nothing

/**
 * A [EsGenerator] that is specialized to represent flow of an evaluation.
 */
class EvalFlow<out V: AbstractType?>(): EsGenerator<NonEmptyNormalOrAbrupt, LanguageType, MaybeAbrupt<V>>() {
    constructor(block: suspend EvalFlow<V>.() -> Unit): this() {
        nextStep = block.createCoroutineUnintercepted(this, this)
    }
    suspend fun <W: AbstractType?> yieldAllOrEmpty(flow: EvalFlow<W>) =
        yieldAll(flow) ?: empty
    fun resume(completion: NonEmptyNormalOrAbrupt): NonEmptyNormalOrAbrupt =
        when {
            hasNext() -> next(completion).toNormal()
            canTakeReturnValue -> takeReturnValue() as NonEmptyNormalOrAbrupt? ?: `null`
            else -> neverHappens()
        }

    /**
     * Note that the function calls [scope]'s [yieldAll] implicitly.
     */
    suspend inline fun <W: AbstractType?> returnIfAbrupt(scope: EvalFlow<W>, rtn: ReturnLambda) =
        scope.yieldAll(this)
            .let { it ?: rtn() }
            .returnIfAbrupt { scope.`return`(it) }
    suspend inline fun neverAbrupt(scope: EvalFlow<@UnsafeVariance V>) =
        returnIfAbrupt(scope) { neverHappens() }
    internal suspend inline fun returnError(kind: ErrorKind, vararg args: String): Nothing =
        `return`(throwError(kind, *args))
}

fun EvalFlow<LanguageType?>.toSyncGenerator() =
    SyncGeneratorType().apply {
        start { this@toSyncGenerator }
    }

/**
 * Takes return value of the [EvalFlow], without storing values that the [EvalFlow] yielded.
 */
fun <V: AbstractType?> EvalFlow<V>.takeReturnValueNoYields(): MaybeAbrupt<V>? {
    while (hasNext()) next()
    return takeReturnValue()
}
