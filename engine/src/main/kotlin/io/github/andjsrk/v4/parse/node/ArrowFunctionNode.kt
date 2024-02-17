package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.ThisMode
import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.*
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
    override fun evaluate() = lazyFlowNoYields {
        instantiateArrowFunction(null)
            .toNormal()
    }
    @EsSpec("InstantiateArrowFunctionExpression")
    @EsSpec("InstantiateAsyncArrowFunctionExpression")
    internal fun instantiateArrowFunction(name: PropertyKey?) =
        OrdinaryFunctionType(name, parameters, body, ThisMode.ARROW, isAsync, isGenerator)
}
