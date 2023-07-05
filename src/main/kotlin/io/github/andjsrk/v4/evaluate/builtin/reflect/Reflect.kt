package io.github.andjsrk.v4.evaluate.builtin.reflect

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.languageValue
import io.github.andjsrk.v4.evaluate.type.DataProperty
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("%Reflect%")
val Reflect = ObjectType(properties=mutableMapOf(
    "defineProperty".languageValue to DataProperty.sealed(defineProperty),
    "defineProperties".languageValue to DataProperty.sealed(defineProperties),
    "getOwnPropertyDescriptor".languageValue to DataProperty.sealed(getOwnPropertyDescriptor),
    "getOwnPropertyDescriptors".languageValue to DataProperty.sealed(getOwnPropertyDescriptors),
    "getOwnKeys".languageValue to DataProperty.sealed(getOwnKeys),
    "getOwnStringKeys".languageValue to DataProperty.sealed(getOwnStringKeys),
    "getOwnSymbolKeys".languageValue to DataProperty.sealed(getOwnSymbolKeys),
    "getOwnerClass".languageValue to DataProperty.sealed(getOwnerClass),
    "getPrototype".languageValue to DataProperty.sealed(getPrototype),
    "isEnumerableProperty".languageValue to DataProperty.sealed(isEnumerableProperty),
    "isExtensible".languageValue to DataProperty.sealed(isExtensible),
    "preventExtensions".languageValue to DataProperty.sealed(preventExtensions),
))
