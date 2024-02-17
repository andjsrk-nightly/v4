package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.toNormal

sealed interface MethodNode: FunctionNode {
    val name: ObjectLiteralKeyNode
    override val body: BlockNode
    override fun evaluate() = evaluateFlexibly(false, false)
    fun evaluateFlexibly(isAsync: Boolean, isGenerator: Boolean) = lazyFlow f@ {
        val name = yieldAll(name.toLanguageTypePropertyKey())
            .orReturn { return@f it }
        OrdinaryFunctionType(name, parameters, body, ThisMode.METHOD, isAsync, isGenerator)
            .toNormal()
    }
}
