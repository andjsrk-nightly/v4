package io.github.andjsrk.v4.parse

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.parse.node.*
import kotlin.reflect.KClass

@EsSpec("IsAnonymousFunctionDefinition")
internal val ExpressionNode.isAnonymous get(): Boolean =
    when (this) {
        is ParenthesizedExpressionNode -> expression.isAnonymous
        is ArrowFunctionNode, is MethodExpressionNode -> true
        is ClassExpressionNode -> name == null
        else -> false
    }

/**
 * A convenient way to find a node that is matched by [Contains](https://tc39.es/ecma262/multipage/syntax-directed-operations.html#sec-static-semantics-contains).
 */
internal fun <N: Node> Node.find(symbol: KClass<N>, predicate: (N) -> Boolean = { true }): N? {
    val baseCondition = lazy { this::class == symbol && predicate(this as N) }

    return when (this) {
        is ClassNode -> parent?.find(symbol, predicate) ?: computedPropertyFind(symbol, predicate)
        // TODO: static initialization block
        is ArrowFunctionNode ->
            symbol.isOneOf(
                SuperNode::class,
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
@EsSpec("ComputedPropertyContains")
internal fun <N: Node> Node.computedPropertyFind(symbol: KClass<N>, predicate: (N) -> Boolean): N? =
    when (this) {
        is ComputedPropertyKeyNode -> find(symbol, predicate)
        is MethodNode -> name.computedPropertyFind(symbol, predicate)
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
        is NormalLexicalDeclarationNode -> bindings.flatMap { it.element.boundNames() }
        is MaybeRestNode -> binding.boundNames()
        is BindingPatternNode -> elements.flatMap { it.boundNames() }
        is UniqueFormalParametersNode -> elements.flatMap { it.boundNames() }
        is ClassDeclarationNode -> name.boundNames()
        is ImportOrExportSpecifierNode -> alias.boundNames()
        is NamedImportDeclarationNode -> specifiers.flatMap { it.boundNames() }
        is NamespaceImportDeclarationNode -> binding.boundNames()
        is DeclarationExportDeclarationNode -> declaration.boundNames()
        else -> emptyList()
    }
internal fun Node.boundStringNames() =
    boundNames().map { it.value }

@EsSpec("IsConstantDeclaration")
internal val DeclarationNode.isConstant get() =
    when (this) {
        is LexicalDeclarationNode -> kind == LexicalDeclarationKind.LET
        is ClassDeclarationNode -> true
        else -> false
    }

@EsSpec("LexicallyDeclaredNames")
internal fun Node.lexicallyDeclaredNames(): List<IdentifierNode> =
    when (this) {
        is DeclarationNode -> boundNames()
        is StatementListNode -> elements.flatMap { it.lexicallyDeclaredNames() }
        is ExpressionNode -> emptyList()
        // TODO: switch statement
        else -> emptyList()
    }

@EsSpec("LexicallyScopedDeclarations")
internal fun Node.lexicallyScopedDeclarations(): List<DeclarationNode> =
    when (this) {
        is DeclarationExportDeclarationNode -> listOf(declaration)
        is ImportDeclarationNode, is ExportDeclarationNode -> emptyList()
        is DeclarationNode -> listOf(this)
        is StatementListNode -> elements.flatMap { it.lexicallyScopedDeclarations() }
        else -> emptyList()
    }

@EsSpec("HasDirectSuper")
internal fun NonAtomicNode.findDirectSuperCall() =
    when (this) {
        is GetterNode -> listOf(body)
        is SetterNode -> listOf(parameter, body)
        is MethodNode -> listOf(parameters, body)
        else -> childNodes
    }
        .mapAsSequence { it?.find(SuperCallNode::class) }
        .foldElvis()

internal fun MethodNode.findDirectSuper() =
    when (this) {
        is GetterNode -> listOf(body)
        is SetterNode -> listOf(parameter, body)
        is MethodNode -> listOf(parameters, body)
    }
        .mapAsSequence { it.find(SuperNode::class) }
        .foldElvis()

@EsSpec("AssignmentTargetType")
internal fun ExpressionNode.isAssignmentTarget(): Boolean =
    when (this) {
        is IdentifierNode -> true
        is SuperPropertyNode -> true
        is MemberExpressionNode -> !isOptionalChain
        is ParenthesizedExpressionNode -> expression.isAssignmentTarget()
        else -> false
    }

/**
 * @return [ObjectLiteralKeyNode] but not [ComputedPropertyKeyNode]
 */
@EsSpec("PropName")
internal fun Node.propName(): ObjectLiteralKeyNode? =
    when (this) {
        is PropertyNode -> key
        is MethodNode -> name
        is FieldNode -> name
        else -> null
    }
        ?.takeIf { it !is ComputedPropertyKeyNode }

@EsSpec("StringValue")
internal val IdentifierNode.stringValue get() =
    value.languageValue

@EsSpec("ExportedNames")
internal fun ModuleNode.exportedNames() =
    elements.asSequence()
        .filterIsInstance<ExportDeclarationNode>()
        .flatMap {
            when (it) {
                is DeclarationExportDeclarationNode -> it.boundNames()
                is NamedExportDeclarationNode -> it.specifiers.flatMap { it.boundNames() }
                else -> emptyList()
            }
        }
        .toList()

@EsSpec("ImportEntriesForModule")
internal fun Node.importEntries(sourceModule: String): List<ImportEntry> =
    when (this) {
        is ImportOrExportSpecifierNode -> listOf(NormalImportEntry(sourceModule, name.value, alias.value))
        is NamedImportDeclarationNode -> specifiers.flatMap { it.importEntries(sourceModule) }
        is NamespaceImportDeclarationNode -> listOf(NamespaceImportEntry(sourceModule, binding.value))
        else -> emptyList()
    }

@EsSpec("ImportEntries")
internal fun ModuleNode.importEntries() =
    elements.asSequence()
        .filterIsInstance<ImportDeclarationNode>()
        .flatMap { it.importEntries(it.moduleSpecifier.value) }
        .toList()

@EsSpec("ExportEntriesForModule")
internal fun Node.exportEntries(sourceModule: String?): List<ExportEntry> =
    when (this) {
        is ImportOrExportSpecifierNode -> {
            val (localName, importName) =
                if (sourceModule == null) name.value to null
                else null to name.value
            listOf(ExportEntry(sourceModule, alias.value, localName, importName))
        }
        is AllReExportDeclarationNode ->
            listOf(ExportEntry(sourceModule, null, null, null))
        is NamedExportDeclarationNode ->
            specifiers.flatMap { it.exportEntries(sourceModule) }
        is DeclarationExportDeclarationNode ->
            declaration.boundStringNames().map {
                ExportEntry(null, it, it, null)
            }
        else -> emptyList()
    }

@EsSpec("ExportEntries")
internal fun ModuleNode.exportEntries() =
    elements.asSequence()
        .filterIsInstance<ExportDeclarationNode>()
        .flatMap { it.exportEntries((it as? ExportDeclarationWithModuleSpecifierNode)?.moduleSpecifier?.value) }
        .toList()

@EsSpec("ModuleRequests")
internal fun ModuleNode.moduleRequests() =
    elements.flatMap {
        when (it) {
            is ImportDeclarationNode -> listOf(it.moduleSpecifier.value)
            is ExportDeclarationWithModuleSpecifierNode -> listOf(it.moduleSpecifier.value)
            else -> emptyList()
        }
    }
