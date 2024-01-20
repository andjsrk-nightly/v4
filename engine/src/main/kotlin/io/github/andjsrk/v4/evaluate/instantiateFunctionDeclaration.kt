package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.error.TypeErrorKind
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.OrdinaryFunctionType
import io.github.andjsrk.v4.mapAsSequence
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.node.BlockNode

@EsSpec("FunctionDeclarationInstantiation")
fun instantiateFunctionDeclaration(func: OrdinaryFunctionType, args: List<LanguageType>): EmptyOrAbrupt {
    val requiredArgCount = func.parameters.requiredParameterCount.toInt()
    if (args.size < requiredArgCount) return throwError(
        TypeErrorKind.REQUIRED_ARGUMENTS_NOT_PROVIDED,
        requiredArgCount.toString(),
        args.size.toString(),
    )
    val calleeContext = runningExecutionContext
    val paramNames = func.parameters.boundStringNames()
    val env = calleeContext.lexicalEnvNotNull
    for (paramName in paramNames) env.createMutableBinding(paramName)
    func.parameters.initializeBindingsBy(args.mapAsSequence { it.toNormal() }.iterator(), env)
        .unwrap() // await or yield can never exist because of syntactic restriction
        .orReturn { return it }
    if (func.body is BlockNode) instantiateBlockDeclaration(func.body, env)
    return empty
}
