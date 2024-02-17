package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.builtin.Map

class MapType(val map: MutableMap<LanguageType, LanguageType>): ObjectType(lazy { Map.instancePrototype })
