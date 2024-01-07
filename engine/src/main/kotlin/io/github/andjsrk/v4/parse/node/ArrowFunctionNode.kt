package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey
import io.github.andjsrk.v4.evaluate.type.toNormal
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
        instantiateArrowFunction(null)
            .toNormal()
    @EsSpec("InstantiateArrowFunctionExpression")
    @EsSpec("InstantiateAsyncArrowFunctionExpression")
    internal fun instantiateArrowFunction(name: PropertyKey?) =
        when {
            isGenerator -> TODO(GENERATOR_FUNCTION_NOT_SUPPORTED_YET)
            else -> OrdinaryFunctionType(
                name,
                parameters,
                body,
                runningExecutionContext.lexicalEnvironment,
                ThisMode.ARROW,
                isAsync,
                isGenerator,
            )
        }
}
