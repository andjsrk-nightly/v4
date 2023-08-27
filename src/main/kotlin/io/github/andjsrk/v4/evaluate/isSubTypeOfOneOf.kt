package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.ClassType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType

fun ClassType.isSubTypeOfOneOf(vararg classes: ClassType): Boolean =
    this in classes || instancePrototype.prototype?.ownerClass?.isSubTypeOfOneOf(*classes) ?: false

fun LanguageType.isInstanceOfOneOf(vararg classes: ClassType) =
    prototype?.ownerClass?.isSubTypeOfOneOf(*classes) ?: false
