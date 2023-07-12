package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.Node

internal inline fun Node.evaluateValueOrReturn(`return`: AbruptReturnLambda) =
    evaluateValue().returnIfAbrupt(`return`)
