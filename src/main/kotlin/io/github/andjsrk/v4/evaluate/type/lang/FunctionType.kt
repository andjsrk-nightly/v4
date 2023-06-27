package io.github.andjsrk.v4.evaluate.type.lang

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.function.Function
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.runningExecutionContext
import io.github.andjsrk.v4.evaluate.type.*

sealed class FunctionType(
    val name: PropertyKey?,
    requiredParameterCount: UInt,
    val env: DeclarativeEnvironment,
): ObjectType(lazy { Function.instancePrototype }) {
    init {
        definePropertyOrThrow(
            "requiredParameterCount".languageValue,
            DataProperty.sealed(requiredParameterCount.toDouble().languageValue),
        )
    }
    val realm = runningExecutionContext.realm
    abstract val isArrow: Boolean
    @EsSpec("[[Call]]")
    abstract fun _call(thisArg: LanguageType, args: List<LanguageType>): NonEmptyNormalOrAbrupt
}
