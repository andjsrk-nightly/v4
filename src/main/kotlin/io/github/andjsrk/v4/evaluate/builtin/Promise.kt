package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.evaluate.unwrap

@EsSpec("%Promise%")
val Promise = BuiltinClassType(
    "Promise",
    Object,
    mutableMapOf(),
    mutableMapOf(),
    constructor(1u) ctor@ { _, args ->
        val executor = args[0]
            .requireToBe<FunctionType> { return@ctor it }
        val promise = PromiseType()
        val (resolve, reject) = promise.createResolveFunction()
        val executorCallRes = executor.call(null, listOf(resolve, reject))
        if (executorCallRes is Completion.Abrupt) reject.call(null, listOf(executorCallRes.value!!)).unwrap()
        promise.toNormal()
    },
)
