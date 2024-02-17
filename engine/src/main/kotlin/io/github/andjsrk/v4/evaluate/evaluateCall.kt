package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("EvaluateCall")
internal fun evaluateCall(value: LanguageType, ref: AbstractType, args: List<LanguageType>): NonEmptyOrAbrupt {
    val thisValue =
        if (ref is Reference && ref.isProperty) ref.getThis()
        else NullType
    val func = value.requireToBe<FunctionType> { return it }
    return func.call(thisValue, args)
}
