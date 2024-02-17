package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.Set

class SetType(val set: MutableSet<LanguageType>): ObjectType(lazy { Set.instancePrototype })
