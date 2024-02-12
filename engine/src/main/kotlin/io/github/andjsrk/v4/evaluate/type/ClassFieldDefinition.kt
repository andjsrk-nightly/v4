package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.evaluate.type.lang.FunctionType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

data class ClassFieldDefinition(val name: PropertyKey, val initializer: FunctionType?): Record
