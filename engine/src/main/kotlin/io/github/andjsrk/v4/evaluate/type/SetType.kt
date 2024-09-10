package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.Set

class SetType(val set: MutableSet<LanguageType>): ObjectType by ObjectType.Impl(lazy { Set.instancePrototype })
