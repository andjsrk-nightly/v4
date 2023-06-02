package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.AbstractType
import io.github.andjsrk.v4.evaluate.type.spec.Completion

fun getValue(v: AbstractType?): Completion {
    return Completion.normal(v) // temp
}
