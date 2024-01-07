package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.ListType
import io.github.andjsrk.v4.evaluate.type.MaybeAbrupt
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.node.TemplateLiteralNode

@EsSpec("ArgumentListEvaluation") // for TemplateLiteral
internal fun evaluateTaggedArguments(taggedTemplate: TemplateLiteralNode): MaybeAbrupt<ListType<LanguageType>> {
    TODO()
}
