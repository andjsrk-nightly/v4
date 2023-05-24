package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.mapAsSequence
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.thenTake
import io.github.andjsrk.v4.util.isOneOf
import kotlin.reflect.KClass

@EsSpec("Contains")
internal fun <N: Node> Node.contains(symbol: KClass<N>, predicate: (N) -> Boolean = { true }) =
    find(symbol, predicate) != null

/**
 * A convenient way to find a node that is matched by [contains].
 */
internal fun <N: Node> Node.find(symbol: KClass<N>, predicate: (N) -> Boolean = { true }): N? {
    val baseCondition = lazy { this::class == symbol && predicate(this as N) }

    return when (this) {
        is ClassNode -> parent?.find(symbol, predicate) ?: computedPropertyFind(symbol, predicate)
        // TODO: static initialization block
        is ArrowFunctionNode ->
            symbol.isOneOf(
                SuperPropertyNode::class,
                SuperCallNode::class,
                ThisNode::class,
            ).thenTake {
                parameters.find(symbol, predicate) ?: body.find(symbol, predicate)
            }
        is NonAtomicNode ->
            if (baseCondition.value) this as N
            else childNodes
                .mapAsSequence { it?.find(symbol, predicate) }
                .foldElvis()
        else -> baseCondition.value.thenTake { this as N }
    }
}
/**
 * @see find
 */
internal fun <N: Node> Node.computedPropertyFind(symbol: KClass<N>, predicate: (N) -> Boolean): N? =
    when (this) {
        is ComputedPropertyKeyNode -> find(symbol, predicate)
        is FixedParametersMethodNode -> name.computedPropertyFind(symbol, predicate)
        is FieldNode -> name.computedPropertyFind(symbol, predicate)
        is ClassNode ->
            elements
                .mapAsSequence { it.computedPropertyFind(symbol, predicate) }
                .foldElvis()
        else -> null
    }

@EsSpec("BoundNames")
internal fun Node.boundNames(): List<IdentifierNode> =
    when (this) {
        is IdentifierNode -> listOf(this)
        is LexicalDeclarationWithoutInitializerNode -> binding.boundNames()
        is MaybeRestNode -> binding.boundNames()
        is BindingPatternNode -> elements.flatMap { it.boundNames() }
        is UniqueFormalParametersNode -> elements.flatMap { it.boundNames() }
        is ClassDeclarationNode -> name.boundNames()
        else -> emptyList()
    }

@EsSpec("HasDirectSuper")
internal fun FixedParametersMethodNode.findDirectSuperCall() =
    when (this) {
        is NonSpecialMethodNode -> listOf(parameters, body)
        is GetterNode -> listOf(body)
        is SetterNode -> listOf(parameter, body)
    }
        .mapAsSequence { it.find(SuperCallNode::class) }
        .foldElvis()

@EsSpec("AssignmentTargetType")
internal fun ExpressionNode.isValidAssignmentTarget(): Boolean =
    when (this) {
        is IdentifierNode -> true
        is MemberExpressionNode -> true
        is ParenthesizedExpressionNode -> expression.isValidAssignmentTarget()
        else -> false
    }
