package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.type.Environment
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.isAnonymous
import io.github.andjsrk.v4.parse.node.*
import io.github.andjsrk.v4.parse.stringValue

@EsSpec("IteratorBindingInitialization")
internal fun UniqueFormalParametersNode.initializeParameterBindings(argsIterator: Iterator<LanguageType>, env: Environment?) =
    elements.initializeParameterBindings(argsIterator, env)
private fun List<MaybeRestNode>.initializeParameterBindings(argsIterator: Iterator<LanguageType>, env: Environment?) =
    EvalFlow {
        for (element in this@initializeParameterBindings) {
            when (element) {
                is NonRestNode ->
                    when (val binding = element.binding) {
                        is IdentifierNode -> {
                            val ref = resolveBinding(binding.stringValue, env)
                            val defaultExpr = element.default
                            var value = when {
                                argsIterator.hasNext() -> argsIterator.next()
                                defaultExpr == null -> neverHappens() // this case will be handled on `instantiateFunctionDeclaration`
                                else -> NullType
                            }
                            if (value == NullType && defaultExpr != null) {
                                value =
                                    if (defaultExpr.isAnonymous) defaultExpr.evaluateWithName(binding.stringValue)
                                    else defaultExpr.evaluateValue().returnIfAbrupt(this) { return@EvalFlow }
                            }
                            ref.putOrInitializeBinding(value, env)
                        }
                        is BindingPatternNode -> TODO()
                        else ->
                            @CompilerFalsePositive
                            neverHappens()
                    }
                is RestNode ->
                    when (val binding = element.binding) {
                        is IdentifierNode -> {
                            val ref = resolveBinding(binding.stringValue, env)
                            val values = mutableListOf<LanguageType>()
                            argsIterator.forEach { values += it }
                            val arr = ImmutableArrayType.from(values)
                            ref.putOrInitializeBinding(arr, env)
                        }
                        is BindingPatternNode -> TODO()
                        else ->
                            @CompilerFalsePositive
                            neverHappens()
                    }
            }
        }
    }
