package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.lang.AsyncGeneratorType.State
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.isOneOf

private val asyncGeneratorNext = method("next") fn@ { thisArg, args ->
    val gen = thisArg
        .requireToBe<AsyncGeneratorType> { return@fn it }
    val value = args.getOptional(0) ?: NullType
    val capability = PromiseType.Capability.new()
    gen.validate(null)
        .orReturnThrow { return@fn rejectedPromise(capability, it) }
    if (gen.state == State.COMPLETED) {
        val iterRes = createIteratorResult(NullType, true)
        capability.resolve.callWithSingleArg(iterRes)
            .unwrap()
        return@fn capability.promise.toNormal()
    }
    gen.enqueue(value.toNormal(), capability)
    if (gen.state.isOneOf(State.SUSPENDED_START, State.SUSPENDED_YIELD)) {
        gen.resume(value.toNormal())
    } else assert(gen.state.isOneOf(State.EXECUTING, State.AWAITING_RETURN))
    capability.promise.toNormal()
}

private val asyncGeneratorClose = method("close", 1u) fn@ { thisArg, args ->
    val gen = thisArg
        .requireToBe<AsyncGeneratorType> { return@fn it }
    val value = args[0]
    val capability = PromiseType.Capability.new()
    gen.validate()
        .orReturnThrow { return@fn rejectedPromise(capability, it) }
    val returnComp = Completion.Return(value)
    gen.enqueue(returnComp, capability)
    when (gen.state) {
        State.SUSPENDED_START,
        State.COMPLETED -> {
            gen.state = State.AWAITING_RETURN
            gen.awaitReturn()
        }
        State.SUSPENDED_YIELD -> {
            gen.resume(returnComp)
        }
        else -> {
            assert(gen.state.isOneOf(State.EXECUTING, State.AWAITING_RETURN))
        }
    }
    capability.promise.toNormal()
}

@EsSpec("%AsyncGeneratorFunction.prototype%")
val AsyncGenerator = BuiltinClassType(
    "AsyncGenerator",
    Object,
    mutableMapOf(),
    mutableMapOf(
        sealedMethod(asyncGeneratorNext),
        sealedMethod(asyncGeneratorClose),
    ),
    constructor { _, _ ->
        TODO()
    },
)
