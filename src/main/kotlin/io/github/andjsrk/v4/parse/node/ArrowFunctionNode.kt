package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.ThisMode
import io.github.andjsrk.v4.evaluate.runningExecutionContext
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ArrowFunctionNode(
    override val parameters: UniqueFormalParametersNode,
    override val body: ConciseBodyNode,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    startRange: Range,
): FunctionExpressionNode {
    override val childNodes get() = listOf(parameters, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::parameters, ::body, ::isAsync, ::isGenerator, ::range)
    override fun evaluate() =
        Completion.Normal(instantiateArrowFunction(null))
    internal fun instantiateArrowFunction(name: PropertyKey?): OrdinaryFunctionType {
        val env = runningExecutionContext.lexicalEnvironment
        return OrdinaryFunctionType(parameters, body, env, ThisMode.ARROW, name)
    }
}
