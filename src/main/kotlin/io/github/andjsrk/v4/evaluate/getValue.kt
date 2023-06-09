package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.evaluate.type.spec.*

@EsSpec("GetValue")
internal fun getValue(v: AbstractType): Completion {
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
