package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.spec.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.*
import io.github.andjsrk.v4.parse.node.StatementListNode

@EsSpec("BlockDeclarationInstantiation")
internal fun instantiateBlockDeclaration(node: StatementListNode, env: DeclarativeEnvironment) {
    for (decl in node.lexicallyScopedDeclarations()) {
        val isConstant = decl.isConstant
        for (name in decl.boundNames()) {
            if (isConstant) env.createImmutableBinding(name.value)
            else env.createMutableBinding(name.value)
        }
    }
}
