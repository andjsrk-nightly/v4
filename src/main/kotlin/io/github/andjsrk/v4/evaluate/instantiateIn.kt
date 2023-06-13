package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.spec.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.boundNames
import io.github.andjsrk.v4.parse.isConstant
import io.github.andjsrk.v4.parse.node.DeclarationNode

fun DeclarationNode.instantiateIn(env: DeclarativeEnvironment, names: List<String> = this.boundNames().map { it.value }) {
    val isConstant = this.isConstant
    for (name in names) {
        if (isConstant) env.createImmutableBinding(name)
        else env.createMutableBinding(name)
    }
}
