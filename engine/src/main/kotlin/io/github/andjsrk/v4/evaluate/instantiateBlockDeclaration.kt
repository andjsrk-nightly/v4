package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.lexicallyScopedDeclarations
import io.github.andjsrk.v4.parse.node.StatementListNode

@EsSpec("BlockDeclarationInstantiation")
fun instantiateBlockDeclaration(node: StatementListNode, env: DeclarativeEnvironment) {
    node.lexicallyScopedDeclarations().forEach {
        it.instantiateIn(env)
    }
}
