package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("GetV")
internal fun LanguageType.getProperty(key: PropertyKey) =
    when (this) {
        NullType -> Completion.`throw`(NullType/* TypeError */)
        is ObjectType -> get(key)
        else -> prototype!!.get(key)
//                       ^^ every primitive types have its own prototype object, so it cannot be `null`
    }
