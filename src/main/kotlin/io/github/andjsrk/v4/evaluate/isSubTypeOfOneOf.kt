package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.lang.ClassType

fun ClassType.isSubTypeOfOneOf(vararg classes: ClassType): Boolean =
    this in classes || instancePrototype.prototype?.ownerClass?.isSubTypeOfOneOf(*classes) ?: false
