package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.isOneOf

private val asyncGeneratorNext = method("next") fn@ { thisArg, args ->
    val gen = thisArg.requireToBe<AsyncGeneratorType> { return@fn it }
    val value = args.getOptional(0) ?: NullType
    val capability = PromiseType.Capability.new()
    gen.validate(null)
        .orReturn { return@fn rejectedPromise(capability, it) }
    if (gen.state == AsyncGeneratorState.COMPLETED) {
        val iterRes = createIteratorResult(NullType, true)
        capability.resolve.call(null, listOf(iterRes))
            .unwrap()
        return@fn capability.promise.toNormal()
    }
    gen.enqueue(value.toNormal(), capability)
    if (gen.state.isOneOf(AsyncGeneratorState.SUSPENDED_START, AsyncGeneratorState.SUSPENDED_YIELD)) {
        gen.resume(value.toNormal())
    } else assert(gen.state.isOneOf(AsyncGeneratorState.EXECUTING, AsyncGeneratorState.AWAITING_RETURN))
    capability.promise.toNormal()
}

@EsSpec("%AsyncGeneratorFunction.prototype%")
val AsyncGenerator = BuiltinClassType(
    "AsyncGenerator",
    Object,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(asyncGeneratorNext),
    ),
    constructor { _, _ ->
        TODO()
    },
)
