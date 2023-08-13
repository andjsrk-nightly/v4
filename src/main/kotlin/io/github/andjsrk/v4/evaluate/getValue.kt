package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.ReferenceErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.StringType

@EsSpec("GetValue")
internal fun getValue(v: AbstractType?): NonEmptyNormalOrAbrupt {
    requireNotNull(v) // requires v should not be null but the function accepts null due to convenience
    if (v is LanguageType) return v.toNormal()
    require(v is Reference)
    if (v.isUnresolvable) {
        require(v.referencedName is StringType)
        return throwError(ReferenceErrorKind.NOT_DEFINED, v.referencedName.value)
    }
    if (v.isProperty) {
        require(v.base is LanguageType)
        if (v.referencedName == null) return normalNull
        return v.base.getProperty(v.referencedName)
    } else {
        val base = v.base
        require(base is Environment)
        require(v.referencedName is StringType)
        return base.getValue(v.referencedName.value)
    }
}
