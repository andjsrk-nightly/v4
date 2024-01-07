package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.toNormal

sealed interface MethodNode: FunctionNode {
    val name: ObjectLiteralKeyNode
    override val body: BlockNode
    override fun evaluate(): MaybeAbrupt<OrdinaryFunctionType> {
        val name = name.toPropertyKey()
            .orReturn { return it }
        val env = runningExecutionContext.lexicalEnvironment
        return OrdinaryFunctionType(name, parameters, body, env, ThisMode.METHOD)
            .toNormal()
    }
}
