package io.github.andjsrk.v4.parse.node.literal

typealias DynamicPrimitiveLiteralRaw = String

abstract class DynamicPrimitiveLiteralNode<Actual>: DynamicLiteralNode<Actual> {
    abstract val raw: DynamicPrimitiveLiteralRaw
    override val value by lazy {
        raw.toActualValue()
    }
    protected abstract fun DynamicPrimitiveLiteralRaw.toActualValue(): Actual
    // primitive literals do not need unsealed nodes
}
