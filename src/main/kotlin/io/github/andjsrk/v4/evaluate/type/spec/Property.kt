package io.github.andjsrk.v4.evaluate.type.spec

import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

data class Property(
    var value: LanguageType,
    var get: ObjectType,
    var set: ObjectType,
    var writable: Boolean,
    var enumerable: Boolean,
    var configurable: Boolean,
): Record {
}
