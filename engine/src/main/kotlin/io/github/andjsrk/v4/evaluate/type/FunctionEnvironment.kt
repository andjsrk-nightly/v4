package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.throwError

@EsSpec("Function Environment Record")
class FunctionEnvironment(
    outer: Environment?,
    var function: FunctionType,
    var thisValue: LanguageType? = null,
): DeclarativeEnvironment(outer) {
    var initialized = false
    @EsSpec("BindThisValue")
    fun bindThisValue(value: LanguageType?): EmptyOrThrow {
        if (initialized) TODO() // what ReferenceError should I throw?
        thisValue = value
        initialized = true
        return empty
    }
    override fun hasThisBinding() =
        function.isMethod
    override fun getThisBinding(): NonEmptyOrThrow {
        assert(function.isMethod)
        if (!initialized) TODO()
        val thisValue = thisValue ?: return throwError(TypeErrorKind.THISARG_NOT_PROVIDED)
        return thisValue.toNormal()
    }

    companion object {
        fun from(func: FunctionType, thisValue: LanguageType? = null) =
            FunctionEnvironment(func.env, func, thisValue)
    }
}
