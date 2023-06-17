package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

fun BindingElementNode.initialize(value: LanguageType, env: DeclarativeEnvironment?): Completion {
    return when (this) {
        is IdentifierNode -> initializeBoundName(stringValue, value, env)
        is BindingPatternNode -> TODO()
    }
}
