package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.parse.node.ExpressionNode
import io.github.andjsrk.v4.parse.node.Node

internal inline fun Node.evaluateOrReturn(`return`: AbruptReturnLambda) =
    evaluate().returnIfAbrupt(`return`)

internal inline fun ExpressionNode.evaluateOrReturn(`return`: AbruptReturnLambda) =
    evaluate().returnIfAbrupt(`return`)
