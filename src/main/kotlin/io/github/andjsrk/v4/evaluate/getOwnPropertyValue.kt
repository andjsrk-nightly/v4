package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.MaybeEmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

internal fun ObjectType.getOwnPropertyValue(key: PropertyKey): MaybeEmptyOrAbrupt {
    if (getOwnProperty(key) == null) return empty
    return get(key)
}
