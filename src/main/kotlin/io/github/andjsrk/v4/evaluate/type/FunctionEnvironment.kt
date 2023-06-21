package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.AbstractFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Function Environment Record")
class FunctionEnvironment(
    outer: Environment,
    var function: AbstractFunctionType,
    var thisValue: LanguageType? = null,
): DeclarativeEnvironment(outer) {
    companion object {
        fun from(func: AbstractFunctionType, thisValue: LanguageType? = null) =
            FunctionEnvironment(func.env, func, thisValue)
    }
}
