package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.Range
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.type.*
import io.github.andjsrk.v4.evaluate.type.lang.*
import io.github.andjsrk.v4.parse.*

sealed class ClassNode: NonAtomicNode {
    abstract val name: IdentifierNode?
    abstract val parent: ExpressionNode?
    abstract val elements: List<ClassElementNode>
    override val childNodes get() = listOf(name, parent) + elements
    override fun toString() =
        stringifyLikeDataClass(::name, ::parent, ::elements, ::range)
    val normalElements get() =
        elements.filterIsInstance<NormalClassElementNode>()
    @EsSpec("ClassDefinitionEvaluation")
    protected fun evaluateTail(shouldBind: Boolean) = lazyFlow f@ {
        val env = runningExecutionContext.lexicalEnv
        val classEnv = DeclarativeEnvironment(env)
        val name = name
        if (shouldBind && name != null) classEnv.createImmutableBinding(name.value)
        val outerPrivEnv = runningExecutionContext.privateEnv
        val classPrivEnv = PrivateEnvironment(outerPrivEnv)
        for (privName in privateBoundIdentifiers()) {
            classPrivEnv.names += privName.stringValue // produces a Private Name
        }
        val parentValue = parent?.let { parentNode ->
            runningExecutionContext.lexicalEnv = classEnv
            val parentRef = yieldAll(parentNode.evaluate())
            runningExecutionContext.lexicalEnv = env
            getValue(parentRef.orReturn { return@f it })
                .orReturnThrow { return@f it }
                .normalizeNull()
                ?.requireToBe<ClassType> { return@f it }
        }
        val parentInstanceProto = parentValue?.instancePrototype ?: Object.instancePrototype
        val ctorNode = constructor
        runningExecutionContext.lexicalEnv = classEnv
        runningExecutionContext.privateEnv = classPrivEnv
        val ctor = yieldAll(
            ctorNode?.evaluate() ?: lazyFlow {
                val defaultCtor = method ctor@ { thisArg, args ->
                    // if constructor is present, call it with thisArg and drop the return value
                    parentValue?.construct(args, thisArg)
                        ?.orReturnThrow { return@ctor it }
                    thisArg.toNormal()
                }
                TODO()
            }
        )
        val staticProps = mutableMapOf<PropertyKey, Property>()
        val instanceProtoProps = mutableMapOf<PropertyKey, Property>()
        val clazz = OrdinaryClassType(
            name?.stringValue,
            parentValue,
            staticProps,
            instanceProtoProps,
            TODO()
        )
        TODO()
    }

    @EsSpec("ConstructorMethod")
    val constructor by lazy {
        elements.find { it.kind == ClassElementKind.CONSTRUCTOR } as ConstructorNode?
    }
}

private class ClassElementCollectResult()
private fun ClassType.collect(elements: List<NormalClassElementNode>) {
    val privStaticMethods = mutableSetOf<PrivateProperty>()
    val privInstanceMethods = mutableSetOf<PrivateProperty>()
    val instanceFields = mutableSetOf<PrivateProperty>()
    val staticElements = mutableSetOf<PrivateProperty>()
    for (elemNode in elements) {
        if (elemNode.kind == ClassElementKind.CONSTRUCTOR) continue
        val elem =
            if (elemNode.isStatic) evaluateClassElement(elemNode, this)
            else evaluateClassElement(elemNode, instancePrototype)
        TODO()
    }
}

private fun evaluateClassElement(element: ClassElementNode, obj: ObjectType) =
    when (element) {
        is FieldNode -> evaluateField(element, obj)
        is MethodNode -> evaluateMethodDefinition(element, obj)
        is EmptyStatementNode -> lazyFlow { empty }
    }

private fun evaluateField(field: FieldNode, obj: ObjectType) = lazyFlow f@ {
    val name = yieldAll(field.name.toPropertyKey())
        .orReturn { return@f it }
    val initializer = field.value?.let { value ->
        OrdinaryFunctionType(
            "".languageValue,
            UniqueFormalParametersNode(emptyList(), Range.dummy),
            BlockNode(
                listOf(
                    ExpressionStatementNode(
                        ReturnNode(value, Range.dummy),
                        null,
                    )
                ),
                Range.dummy,
            ),
            ThisMode.METHOD,
            runningExecutionContext.lexicalEnvNotNull,
            runningExecutionContext.privateEnv,
        )
            .apply {
                homeObject = obj
            }
    }
    ClassFieldDefinition(name, initializer)
        .toWideNormal()
}
