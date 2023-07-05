package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

@EsSpec("Function Environment Record")
class FunctionEnvironment(
    outer: Environment,
    var function: FunctionType,
    var thisValue: LanguageType? = null,
): DeclarativeEnvironment(outer) {
    var initialized = false
    @EsSpec("BindThisValue")
    fun bindThisValue(value: LanguageType): EmptyOrAbrupt {
        if (initialized) TODO() // what ReferenceError should I throw?
        thisValue = value
        initialized = true
        return empty
    }

    companion object {
        fun from(func: FunctionType, thisValue: LanguageType? = null) =
            FunctionEnvironment(func.env, func, thisValue)
    }
}
