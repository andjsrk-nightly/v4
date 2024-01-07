package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Stack
import io.github.andjsrk.v4.evaluate.type.Module

@EsSpec("execution context stack")
val executionContextStack = Stack<ExecutionContext>()

@EsSpec("running execution context")
inline val runningExecutionContext get() =
    executionContextStack.top

@EsSpec("GetActiveScriptOrModule")
fun getActiveModule(): Module? {
    if (executionContextStack.isEmpty()) return null
    return executionContextStack.firstOrNull { it.module != null }?.module
}
