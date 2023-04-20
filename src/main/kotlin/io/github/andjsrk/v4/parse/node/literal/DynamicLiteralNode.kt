package io.github.andjsrk.v4.parse.node.literal

interface DynamicLiteralNode<Actual>: LiteralNode {
    val value: Actual
    interface Unsealed<Actual>: LiteralNode.Unsealed {
        override fun toSealed(): DynamicLiteralNode<Actual>
    }
}
