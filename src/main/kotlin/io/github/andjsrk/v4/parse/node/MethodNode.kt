package io.github.andjsrk.v4.parse.node

/**
 * Represents a method without keywords such as `async`.
 */
sealed interface MethodNode: MethodLikeNode, FunctionNode
