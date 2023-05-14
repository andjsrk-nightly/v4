package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.util.isOneOf
import kotlin.reflect.KClass

@EsSpec("Contains")
fun <N: Node> Node.contains(symbol: KClass<N>, predicate: (N) -> Boolean = { true }): Boolean {
    val baseCondition = lazy { this::class == symbol && predicate(this as N) }

    return when (this) {
        is ClassNode -> {
            val parent = parent
            if (parent != null && parent.contains(symbol, predicate)) return true
            computedPropertyContains(symbol, predicate)
        }
        // TODO: static initialization block
        is ArrowFunctionNode ->
            symbol.isOneOf(
                SuperPropertyNode::class,
                SuperCallNode::class,
                ThisNode::class,
            ) && (
                parameters.contains(symbol, predicate) || body.contains(symbol, predicate)
            )
        is NonAtomicNode -> baseCondition.value || childNodes.any { it?.contains(symbol, predicate) ?: false }
        else -> baseCondition.value
    }
}

@EsSpec("ComputedPropertyContains")
fun <N: Node> Node.computedPropertyContains(symbol: KClass<N>, predicate: (N) -> Boolean): Boolean =
    when (this) {
        is ComputedPropertyKeyNode -> this.contains(symbol, predicate)
        is MethodNode -> name.computedPropertyContains(symbol, predicate)
        is FieldNode -> name.computedPropertyContains(symbol, predicate)
        is ClassNode -> elements.any { it.computedPropertyContains(symbol, predicate) }
        else -> false
    }

@EsSpec("BoundNames")
fun Node.boundNames(): List<IdentifierNode> =
    when (this) {
        is IdentifierNode -> listOf(this)
        is LexicalDeclarationWithoutInitializerNode -> binding.boundNames()
        is MaybeRestNode -> binding.boundNames()
        is BindingPatternNode -> elements.flatMap { it.boundNames() }
        is FormalParametersNode -> elements.flatMap { it.boundNames() }
        is ClassDeclarationNode -> name.boundNames()
        else -> emptyList()
    }

@EsSpec("HasDirectSuper")
fun MethodLikeNode.hasDirectSuper() =
    when (this) {
        is MethodNode -> listOf(parameters, body)
        is GetterNode -> listOf(body)
        is SetterNode -> listOf(parameter, body)
    }
        .any { it.contains(SuperCallNode::class) }
