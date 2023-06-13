package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.spec.Completion
import io.github.andjsrk.v4.evaluate.type.spec.ModuleEnvironment
import io.github.andjsrk.v4.parse.node.ModuleNode

/**
 * Note that the evaluator is a singleton due to global access to states of evaluator from some operations.
 */
object Evaluator {
    @EsSpec("running execution context")
    val runningExecutionContext = ExecutionContext(
        ModuleEnvironment(),
    )
    fun evaluate(module: ModuleNode): Completion {
        instantiateBlockDeclaration(module, runningExecutionContext.lexicalEnvironment)
        return module.evaluate()
    }
    fun cleanup() {
        runningExecutionContext.lexicalEnvironment = ModuleEnvironment()
    }
}

inline val runningExecutionContext get() = Evaluator.runningExecutionContext
