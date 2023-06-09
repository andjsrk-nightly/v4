package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.spec.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.node.ModuleNode

/**
 * Note that the evaluator is a singleton due to global access to states of evaluator from some operations.
 */
object Evaluator {
    @EsSpec("running execution context")
    val runningExecutionContext = ExecutionContext(
        DeclarativeEnvironment(),
    )
    fun evaluate(module: ModuleNode) =
        module.evaluate()
}
