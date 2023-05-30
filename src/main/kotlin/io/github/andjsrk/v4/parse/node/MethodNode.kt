package io.github.andjsrk.v4.parse.node

sealed interface MethodNode: FunctionWithoutParameterNode {
    val name: ObjectLiteralKeyNode
    override val body: BlockNode
}
