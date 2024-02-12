package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.lang.PrivateName
import io.github.andjsrk.v4.evaluate.type.toWideNormal
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class IdentifierNode(
    val value: String,
    val isPrivateName: Boolean = false,
    override val range: Range,
): ExpressionNode, ObjectLiteralKeyNode, BindingElementNode {
    constructor(value: String, range: Range): this(value, false, range)
    override fun toString() =
        stringifyLikeDataClass(::value, ::range)
    override fun evaluate() = lazyFlowNoYields {
        if (isPrivateName) toPrivateName().toWideNormal()
        else resolveBinding(value.languageValue)
    }
    /**
     * Converts the [IdentifierNode] into a [PrivateName]
     * assuming that the identifier is valid as a private name.
     */
    fun toPrivateName(): PrivateName {
        require(isPrivateName) { "The identifier '$value' is not a private name" }
        return PrivateName(value)
    }
}
