package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Stack

@EsSpec("execution context stack")
internal val executionContextStack = Stack<ExecutionContext>()

@EsSpec("running execution context")
internal inline val runningExecutionContext get() =
    executionContextStack.top
