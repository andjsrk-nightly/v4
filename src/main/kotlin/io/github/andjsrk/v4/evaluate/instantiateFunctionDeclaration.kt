package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.EmptyOrAbrupt
import io.github.andjsrk.v4.evaluate.type.empty
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.node.BlockNode

@EsSpec("FunctionDeclarationInstantiation")
internal fun instantiateFunctionDeclaration(func: OrdinaryFunctionType, args: List<LanguageType>): EmptyOrAbrupt {
    val requiredArgCount = func.parameters.requiredParameterCount.toInt()
    if (args.size < requiredArgCount) return throwError(
        TypeErrorKind.REQUIRED_ARGUMENTS_NOT_PROVIDED,
        requiredArgCount.toString(),
        args.size.toString(),
    )
    val calleeContext = runningExecutionContext
    val paramNames = func.parameters.boundStringNames()
    val env = calleeContext.lexicalEnvironment
    for (paramName in paramNames) env.createMutableBinding(paramName)
    func.parameters.initializeParameterBindings(args.iterator(), env)
        .returnIfAbrupt { return it }
    if (func.body is BlockNode) instantiateBlockDeclaration(func.body, env)
    return empty
}
