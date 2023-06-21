package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.*

@EsSpec("CopyDataProperties")
internal fun copyDataProperties(
    target: ObjectType,
    source: LanguageType,
    exclude: List<PropertyKey> = emptyList(),
): EmptyOrAbrupt {
    if (source == NullType) return Completion.Normal.empty
    val keys = source.ownPropertyKeys()
    for (key in keys) {
        if (key in exclude) continue
        val desc = source.getOwnProperty(key)
        if (desc != null && desc.enumerable) {
            val value = returnIfAbrupt(source.getProperty(key)) { return it }
            target.createDataProperty(key, value)
        }
    }
    return Completion.Normal.empty
}
