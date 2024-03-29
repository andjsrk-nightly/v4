package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.ThisMode
import io.github.andjsrk.v4.evaluate.lazyFlowNoYields
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class MethodExpressionNode(
    override val parameters: UniqueFormalParametersNode,
    override val body: BlockNode,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    startRange: Range,
): FunctionExpressionNode {
    override val childNodes get() = listOf<Node?>(parameters, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::parameters, ::body, ::isAsync, ::isGenerator, ::range)
    override fun evaluate() = lazyFlowNoYields {
        instantiateMethod(null)
            .toNormal()
    }
    @EsSpec("InstantiateOrdinaryFunctionExpression")
    @EsSpec("InstantiateAsyncFunctionExpression")
    @EsSpec("InstantiateGeneratorFunctionExpression")
    @EsSpec("InstantiateAsyncGeneratorFunctionExpression")
    internal fun instantiateMethod(name: PropertyKey?) =
        OrdinaryFunctionType(name, parameters, body, ThisMode.METHOD, isAsync, isGenerator)
}
