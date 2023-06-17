package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetValue")
internal fun getValue(v: AbstractType?): Completion {
    requireNotNull(v) // requires v should not be null but the function accepts null due to convenience
    if (v is LanguageType) return Completion.normal(v)
    require(v is Reference)
    if (v.isProperty) {
        require(v.base is LanguageType)
        val baseObj = returnIfAbrupt<ObjectType>(v.base.toObject()) { return it }
        TODO()
    } else {
        val base = v.base
        require(base is Environment)
        require(v.referencedName is StringType)
        return base.getValue(v.referencedName.value)
    }
}
