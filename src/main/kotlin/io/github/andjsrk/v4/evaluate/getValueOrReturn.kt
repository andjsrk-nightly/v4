package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.AbstractType

internal inline fun getValueOrReturn(v: AbstractType?, `return`: AbruptReturnLambda) =
    returnIfAbrupt(getValue(v), `return`)
