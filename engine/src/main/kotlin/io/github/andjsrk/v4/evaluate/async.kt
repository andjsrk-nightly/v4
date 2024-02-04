package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

fun await(value: LanguageType) = lazyFlow {
    val context = runningExecutionContext
    val promise = PromiseType.resolve(value)
    val onFulfilled = functionWithoutThis("", 1u) { args ->
        val value = args[0]
        executionContextStack.addTop(context)
        context.codeEvaluationState?.next(value.toNormal())
        normalNull
    }
    val onRejected = functionWithoutThis("", 1u) { args ->
        val reason = args[0]
        executionContextStack.addTop(context)
        context.codeEvaluationState?.next(Completion.Throw(reason))
        normalNull
    }
    promise.then(onFulfilled, onRejected)
    executionContextStack.removeTop()
    val callerContext = runningExecutionContext
    callerContext.codeEvaluationState?.next()
    yield(normalNull) ?: normalNull
}

/**
 * The function is intended to be used on [orReturn] call.
 *
 * @sample rejectedPromiseSample
 */
fun rejectedPromise(capability: PromiseType.Capability, reason: Completion.Throw) =
    capability.promise.toNormal().also {
        capability.reject.call(null, listOf(reason.value))
            .unwrap()
    }
private fun rejectedPromiseSample(): Completion.Normal<PromiseType> {
    val capability = PromiseType.Capability.new()
    var someCompletion: NonEmptyOrThrow = TODO()
    someCompletion
        .orReturnThrow { return rejectedPromise(capability, it) }
}
