package io.github.andjsrk.v4.parse.node

interface BlockNode: ComplexNode {
    val statements: List<StatementNode>
}
