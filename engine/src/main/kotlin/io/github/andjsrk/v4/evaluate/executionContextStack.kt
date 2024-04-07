package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.Module

@EsSpec("execution context stack")
val executionContextStack = Stack<ExecutionContext>()

@EsSpec("running execution context")
inline val runningExecutionContext get() =
    executionContextStack.top

inline fun <R> withTemporalCtx(ctx: ExecutionContext, block: () -> R) =
    withTemporalState(
        { executionContextStack.addTop(ctx) },
        { executionContextStack.removeTop() },
        block,
    )
inline fun <R> withTemporalLexicalEnv(env: DeclarativeEnvironment, block: () -> R) =
    withTemporalValue(runningExecutionContext::lexicalEnv, env, block)

@EsSpec("GetActiveScriptOrModule")
fun getActiveModule(): Module? {
    if (executionContextStack.isEmpty()) return null
    return executionContextStack.firstOrNull { it.module != null }?.module
}
