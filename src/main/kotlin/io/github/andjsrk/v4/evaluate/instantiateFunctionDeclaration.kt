package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.node.BlockNode

@EsSpec("FunctionDeclarationInstantiation")
internal fun instantiateFunctionDeclaration(func: OrdinaryFunctionType, args: List<LanguageType>): EmptyOrAbrupt {
    val calleeContext = runningExecutionContext
    val paramNames = func.parameters.boundStringNames()
    val env = calleeContext.lexicalEnvironment
    for (paramName in paramNames) env.createMutableBinding(paramName)
    // TODO: initialize bindings
    if (func.body is BlockNode) instantiateBlockDeclaration(func.body, env)
    return empty
}
