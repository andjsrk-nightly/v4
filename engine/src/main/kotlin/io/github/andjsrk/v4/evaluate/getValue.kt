package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("GetValue")
internal fun getValue(value: EvaluationResult?): NonEmptyOrThrow {
    requireNotNull(value) // v should not be null but the function accepts null for convenience
    if (value is LanguageType) return value.toNormal()
    require(value is Reference)
    if (value.isUnresolvable) {
        require(value.referencedName is StringType)
        return throwError(ReferenceErrorKind.NOT_DEFINED, value.referencedName.nativeValue)
    }
    if (value.isProperty) {
        require(value.base is LanguageType)
        val refName = value.referencedName ?: return normalNull
        if (refName is PrivateName) return value.base.privateGet(refName)
        return value.base.getProperty(refName)
    } else {
        val base = value.base
        require(base is Environment)
        require(value.referencedName is StringType)
        return base.getBindingValue(value.referencedName.nativeValue)
    }
}
