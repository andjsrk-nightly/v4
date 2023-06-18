package io.github.andjsrk.v4.evaluate.builtin

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("global object")
val global = ObjectType.createBasic().apply {
    set("global".languageValue, this)
    // TODO: function properties
    set("Object".languageValue, Object)
    set("String".languageValue, String)
}
