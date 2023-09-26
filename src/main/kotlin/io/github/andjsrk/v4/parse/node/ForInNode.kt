package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.LanguageType
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.parse.boundStringNames
import io.github.andjsrk.v4.parse.stringifyLikeDataClass

class ForInNode(
    val declaration: LexicalDeclarationWithoutInitializerNode,
    val target: ExpressionNode,
    override val body: StatementNode,
    startRange: Range,
): ForNode {
    override val childNodes get() = listOf(declaration, target, body)
    override val range = startRange..body.range
    override fun toString() =
        stringifyLikeDataClass(::declaration, ::target, ::body, ::range)
    override fun evaluateLoop(): NonEmptyNormalOrAbrupt {
        val iter = evaluateHead()
            .orReturn { return it }
            .toSequence()
            .iterator()
        return evaluateBody(iter)
    }
    private fun evaluateHead(): MaybeAbrupt<IteratorRecord> {
        val oldEnv = runningExecutionContext.lexicalEnvironment
        val uninitializedBoundNames = declaration.boundStringNames()
        if (uninitializedBoundNames.isNotEmpty()) {
            val newEnv = DeclarativeEnvironment(oldEnv)
            uninitializedBoundNames.forEach {
                newEnv.createMutableBinding(it)
            }
            runningExecutionContext.lexicalEnvironment = newEnv
        }
        val targetValueOrAbrupt = target.evaluateValue()
        runningExecutionContext.lexicalEnvironment = oldEnv
        val targetValue = targetValueOrAbrupt
            .orReturn { return it }
        return IteratorRecord.from(targetValue)
    }
    @EsSpec("ForIn/OfBodyEvaluation")
    private fun evaluateBody(iterator: Iterator<NonEmptyNormalOrAbrupt>): NonEmptyNormalOrAbrupt {
        val oldEnv = runningExecutionContext.lexicalEnvironment
        var res: LanguageType = NullType
        while (true) {
            val nextValue = iterator.next()
                .orReturn { return it }
            val iteratorEnv = DeclarativeEnvironment(oldEnv)
            declaration.instantiateIn(iteratorEnv)
            runningExecutionContext.lexicalEnvironment = iteratorEnv
            declaration.binding.initializeBy(nextValue, iteratorEnv)
                .orReturn {
                    runningExecutionContext.lexicalEnvironment = oldEnv
                    // TODO: Perform step 6.i.iv.2 (IteratorClose)
                    return it
                }
            val stmtRes = body.evaluate()
            runningExecutionContext.lexicalEnvironment = oldEnv
            if (!continueLoop(stmtRes)) {
                require(stmtRes is Completion.Abrupt)
                val abrupt = updateEmpty(stmtRes, res)
                // TODO: Perform step 6.l.ii.4 (IteratorClose)
                return abrupt
            }
            val stmtResValue = stmtRes.value as LanguageType? // we can sure that the completion is either a normal completion or a continue completion
            if (stmtResValue != null) res = stmtResValue
            if (iterator.not { hasNext() }) return res.toNormal()
        }
    }
}
