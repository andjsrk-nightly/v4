package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.isConstant
import io.github.andjsrk.v4.parse.node.DeclarationNode

internal fun DeclarationNode.instantiateIn(env: DeclarativeEnvironment, names: List<String> = this.boundStringNames()) {
    val isConstant = this.isConstant
    for (name in names) {
        if (isConstant) env.createImmutableBinding(name)
        else env.createMutableBinding(name)
    }
}
