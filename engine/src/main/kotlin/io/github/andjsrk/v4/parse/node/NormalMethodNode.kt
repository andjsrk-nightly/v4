package io.github.andjsrk.v4.parse.node

sealed interface NormalMethodNode: MethodNode, SpecialFunctionNode {
    override fun evaluate() = evaluateFlexibly(isAsync, isGenerator)
}
