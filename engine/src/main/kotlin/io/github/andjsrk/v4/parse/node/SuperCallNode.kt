package io.github.andjsrk.v4.parse.node

class SuperCallNode(
    override val callee: SuperNode,
    arguments: ArgumentsNode,
): CallNode(callee, arguments)
