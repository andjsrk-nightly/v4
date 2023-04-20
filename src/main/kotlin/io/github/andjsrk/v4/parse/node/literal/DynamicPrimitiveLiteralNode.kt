package io.github.andjsrk.v4.parse.node.literal

typealias DynamicPrimitiveLiteralRaw = String

abstract class DynamicPrimitiveLiteralNode<Actual>: DynamicLiteralNode<Actual> {
    abstract val raw: DynamicPrimitiveLiteralRaw
    override val value by lazy {
        raw.toActualValue()
    }
    protected abstract fun DynamicPrimitiveLiteralRaw.toActualValue(): Actual
    abstract class Unsealed<Actual>: DynamicLiteralNode.Unsealed<Actual> {
        var raw = ""
        abstract override fun toSealed(): DynamicPrimitiveLiteralNode<Actual>
    }
}
