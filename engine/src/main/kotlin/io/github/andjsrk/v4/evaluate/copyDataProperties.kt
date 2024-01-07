package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("CopyDataProperties")
internal fun copyDataProperties(
    target: ObjectType,
    source: LanguageType,
    exclude: List<PropertyKey> = emptyList(),
): EmptyOrAbrupt {
    if (source == NullType) return empty
    val keys = source.ownPropertyKeys()
    for (key in keys) {
        if (key in exclude) continue
        val desc = source.getOwnProperty(key)
            .orReturn { return it }
        if (desc != null && desc.enumerable) {
            val value = source.getProperty(key)
                .orReturn { return it }
            target.createDataProperty(key, value)
        }
    }
    return empty
}
