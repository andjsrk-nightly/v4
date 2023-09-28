package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.toNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ObjectMethodNode(
    override val name: ObjectLiteralKeyNode,
    override val parameters: UniqueFormalParametersNode,
    override val body: BlockNode,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    startRange: Range,
): NormalMethodNode, ObjectElementNode {
    override val childNodes get() = listOf(name, parameters, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::name, ::parameters, ::body, ::isAsync, ::isGenerator, ::range)
    override fun evaluate(): MaybeAbrupt<OrdinaryFunctionType> {
        val name = name.toPropertyKey()
            .orReturn { return it }
        val env = runningExecutionContext.lexicalEnvironment
        return OrdinaryFunctionType(name, parameters, body, env, ThisMode.METHOD)
            .toNormal()
    }
}
