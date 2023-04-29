package io.github.andjsrk.v4.parse.node

sealed class FunctionNode(
    val parameters: List<MaybeRestNode>,
    val body: Node, // body can be an expression in arrow functions
    val isAsync: Boolean,
    val isGenerator: Boolean,
): Node
