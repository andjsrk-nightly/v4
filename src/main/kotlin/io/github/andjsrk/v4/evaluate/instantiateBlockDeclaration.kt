package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.spec.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.lexicallyScopedDeclarations
import io.github.andjsrk.v4.parse.node.StatementListNode

@EsSpec("BlockDeclarationInstantiation")
internal fun instantiateBlockDeclaration(node: StatementListNode, env: DeclarativeEnvironment) {
    for (decl in node.lexicallyScopedDeclarations()) decl.instantiateIn(env)
}
