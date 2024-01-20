package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.toNormal

sealed interface MethodNode: FunctionNode {
    val name: ObjectLiteralKeyNode
    override val body: BlockNode
    override fun evaluate() = lazyFlow f@ {
        val name = yieldAll(name.toPropertyKey())
            .orReturn { return@f it }
        val env = runningExecutionContext.lexicalEnvNotNull
        OrdinaryFunctionType(name, parameters, body, env, ThisMode.METHOD)
            .toNormal()
    }
}
