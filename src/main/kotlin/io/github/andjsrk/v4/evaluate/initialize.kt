package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.CompilerFalsePositive
import io.github.andjsrk.v4.evaluate.type.DeclarativeEnvironment
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.neverHappens
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

internal fun BindingElementNode.initialize(value: LanguageType, env: DeclarativeEnvironment?): EmptyOrAbrupt {
    return when (this) {
        is IdentifierNode -> initializeBoundName(stringValue, value, env)
        is BindingPatternNode -> TODO()
        else ->
            @CompilerFalsePositive
            neverHappens()
    }
}
