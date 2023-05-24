package io.github.andjsrk.v4.parse.node

sealed interface FixedParametersMethodNode: FunctionNode {
    val name: ObjectLiteralKeyNode
    override val body: BlockNode
}
