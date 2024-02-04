package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal

private val resolve = functionWithoutThis("resolve", 1u) fn@ { args ->
    PromiseType.resolve(args[0])
        .toNormal()
}

private val reject = functionWithoutThis("reject") fn@ { args ->
    val capability = PromiseType.Capability.new()
    capability.reject.call(null, listOf(args.getOptional(0) ?: NullType))
    capability.promise
        .toNormal()
}

val then = method("then", 1u) fn@ { thisArg, args ->
    val promise = thisArg
        .requireToBe<PromiseType> { return@fn it }
    val onFulfilled = args[0]
        .normalizeNull()
        ?.requireToBe<FunctionType> { return@fn it }
    val onRejected = args.getOptional(1)
        ?.requireToBe<FunctionType> { return@fn it }
    val capability = PromiseType.Capability.new()
    promise.then(onFulfilled, onRejected, capability)
        .toNormal()
}

private val catch = method("catch", 1u) fn@ { thisArg, args ->
    val promise = thisArg
        .requireToBe<PromiseType> { return@fn it }
    val onRejected = args[0]
        .normalizeNull()
        ?.requireToBe<FunctionType> { return@fn it }
    then.call(promise, listOf(NullType, onRejected ?: NullType))
}

private val finally = method("finally", 1u) fn@ { thisArg, args ->
    val promise = thisArg
        .requireToBe<PromiseType> { return@fn it }
    val onFinally = args[0]
        .normalizeNull()
        ?.requireToBe<FunctionType> { return@fn it }
        ?: return@fn then.call(promise, listOf(NullType, NullType))
    val thenFinally = functionWithoutThis("", 1u) then@ { args ->
        val value = args[0]
        val res = onFinally.call()
            .orReturnThrow { return@then it }
        val p = PromiseType.resolve(res)
        val valueProvider = functionWithoutThis("") {
            value.toNormal()
        }
        then.call(p, listOf(valueProvider))
    }
    val catchFinally = functionWithoutThis("", 1u) catch@ { args ->
        val reason = args[0]
        val res = onFinally.call()
            .orReturnThrow { return@catch it }
        val p = PromiseType.resolve(res)
        val thrower = functionWithoutThis("") {
            Completion.Throw(reason)
        }
        then.call(p, listOf(thrower))
    }
    then.call(promise, listOf(thenFinally, catchFinally))
}

@EsSpec("%Promise%")
val Promise = BuiltinClassType(
    "Promise",
    Object,
    mutableMapOf(
        sealedMethod(resolve),
        sealedMethod(reject),
    ),
    mutableMapOf(
        sealedMethod(then),
        sealedMethod(catch),
        sealedMethod(finally),
    ),
    constructor(1u) ctor@ { _, args ->
        val executor = args[0]
            .requireToBe<FunctionType> { return@ctor it }
        val promise = PromiseType()
        val (resolve, reject) = promise.createResolveRejectFunction()
        val executorCallRes = executor.call(null, listOf(resolve, reject))
        if (executorCallRes is Completion.Abrupt) reject.call(null, listOf(executorCallRes.value!!)).unwrap()
        promise.toNormal()
    },
)
