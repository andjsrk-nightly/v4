package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.Function
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.runningExecutionContext
import io.github.andjsrk.v4.evaluate.type.*

sealed class AbstractFunctionType(
    val name: PropertyKey?,
    requiredParameterCount: UInt,
    val env: DeclarativeEnvironment,
): ObjectType(Function.instancePrototype) {
    init {
        definePropertyOrThrow(
            "length".languageValue,
            DataProperty.sealed(requiredParameterCount.toDouble().languageValue),
        )
    }
    val realm = runningExecutionContext.realm
    @EsSpec("[[Call]]")
    abstract fun _call(thisArg: LanguageType, args: List<LanguageType>): Completion
}
