package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.parse.ClassElementKind
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

sealed class ClassNode: NonAtomicNode {
    abstract val name: IdentifierNode?
    abstract val parent: ExpressionNode?
    abstract val elements: List<ClassElementNode>
    override val childNodes get() = listOf(name, parent) + elements
    override fun toString() =
        stringifyLikeDataClass(::name, ::parent, ::elements, ::range)

    @EsSpec("ConstructorMethod")
    val constructor by lazy {
        elements.find { it.kind == ClassElementKind.CONSTRUCTOR } as ConstructorNode?
    }
}
