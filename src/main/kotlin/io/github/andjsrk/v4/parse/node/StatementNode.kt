package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.evaluate.EvalFlow
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

sealed interface StatementNode: Node {
    override fun evaluate(): EvalFlow<LanguageType?>
}
