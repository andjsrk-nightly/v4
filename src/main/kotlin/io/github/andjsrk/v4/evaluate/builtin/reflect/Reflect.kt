package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("%Reflect%")
val Reflect = ObjectType(properties=mutableMapOf(
    sealedData(::defineProperty),
    sealedData(::defineProperties),
    sealedData(::getOwnPropertyDescriptor),
    sealedData(::getOwnPropertyDescriptors),
    sealedData(::getOwnKeys),
    sealedData(::getOwnStringKeys),
    sealedData(::getOwnSymbolKeys),
    sealedData(::getOwnerClass),
    sealedData(::getPrototype),
    sealedData(::isEnumerableProperty),
    sealedData(::isExtensible),
    sealedData(::preventExtensions),
))
