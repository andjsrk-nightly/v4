package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.sealedData
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("%Reflect%")
val Reflect = ObjectType(properties=mutableMapOf(
    "defineProperty".sealedData(defineProperty),
    "defineProperties".sealedData(defineProperties),
    "getOwnPropertyDescriptor".sealedData(getOwnPropertyDescriptor),
    "getOwnPropertyDescriptors".sealedData(getOwnPropertyDescriptors),
    "getOwnKeys".sealedData(getOwnKeys),
    "getOwnStringKeys".sealedData(getOwnStringKeys),
    "getOwnSymbolKeys".sealedData(getOwnSymbolKeys),
    "getOwnerClass".sealedData(getOwnerClass),
    "getPrototype".sealedData(getPrototype),
    "isEnumerableProperty".sealedData(isEnumerableProperty),
    "isExtensible".sealedData(isExtensible),
    "preventExtensions".sealedData(preventExtensions),
))
