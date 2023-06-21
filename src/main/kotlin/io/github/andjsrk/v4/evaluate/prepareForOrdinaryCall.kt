package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.FunctionEnvironment
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType

internal fun FunctionType.prepareForOrdinaryCall(): ExecutionContext {
    val callerContext = runningExecutionContext
    val calleeContext = ExecutionContext(FunctionEnvironment.from(this), realm, function=this)
    TODO()
}
