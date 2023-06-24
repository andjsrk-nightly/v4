package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Stack
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.parse.node.ModuleNode

/**
 * Note that the evaluator is a singleton due to global access to states of evaluator from some operations.
 */
object Evaluator {
    @EsSpec("execution context stack")
    val executionContextStack = Stack<ExecutionContext>()
    fun evaluate(module: ModuleNode): Completion<*> {
        instantiateBlockDeclaration(module, runningExecutionContext.lexicalEnvironment)
        return module.evaluate()
    }
}

inline val executionContextStack
    get() = Evaluator.executionContextStack

@EsSpec("running execution context")
inline val runningExecutionContext get() =
    executionContextStack.top
