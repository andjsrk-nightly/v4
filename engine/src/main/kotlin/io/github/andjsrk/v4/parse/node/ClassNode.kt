package io.github.andjsrk.v4.parse.node

import io.github.andjsrk.v4.*
import io.github.andjsrk.v4.evaluate.*
import io.github.andjsrk.v4.evaluate.builtin.Object
import io.github.andjsrk.v4.evaluate.type.*
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
    protected fun evaluateTail() = lazyFlow f@ {
        val env = runningExecutionContext.lexicalEnv
        val classEnv = DeclarativeEnvironment(env)
        val name = name?.value
        if (name != null) classEnv.createImmutableBinding(name)
        val outerPrivEnv = runningExecutionContext.privateEnv
        val classPrivEnv = PrivateEnvironment(outerPrivEnv)
        privateBoundIdentifiers().forEach {
            classPrivEnv.names += it.toPrivateName()
        }
        val parentNode = parent
        val parentValue =
            if (parentNode == null) Object
            else
                withTemporalValue(runningExecutionContext::lexicalEnv, classEnv) {
                    yieldAll(parentNode.evaluate())
                }
                    .orReturn { return@f it }
                    .let(::getValue)
                    .orReturnThrow { return@f it }
                    .normalizeNull()
                    ?.requireToBe<ClassType> { return@f it }
        val ctorNode = constructor
        withTemporalState(
            {
                runningExecutionContext.lexicalEnv = classEnv
                runningExecutionContext.privateEnv = classPrivEnv
            },
            {
                runningExecutionContext.lexicalEnv = env
                runningExecutionContext.privateEnv = outerPrivEnv
            },
        ) {

            val ctor = ctorNode?.evaluate()?.let { yieldAll(it) }
                ?.orReturn { return@f it }
                ?: method("constructor") ctor@ { thisArg, args ->
                    // if constructor is present, call it with thisArg and drop the return value
                    parentValue?.construct(args, thisArg)
                        ?.orReturnThrow { return@ctor it }
                    thisArg.toNormal()
                }
            val staticProps = mutableMapOf<PropertyKey, Property>()
            val instanceProtoProps = mutableMapOf<PropertyKey, Property>()
            val clazz = OrdinaryClassType(
                name?.languageValue,
                parentValue,
                staticProps,
                instanceProtoProps,
                ctor,
            )
            val res = ClassElementCollectResult(
                privateInstanceMethods=clazz.privateInstanceMethods,
                instanceFields=clazz.instanceFields,
            )
            yieldAll(clazz.collect(normalElements, res))
                .orReturn { return@f it }
            if (name != null) classEnv.initializeBinding(name, clazz)
            res.privateStaticMethods.forEach { (_, m) ->
                clazz.addPrivateMethodOrAccessor(m).unwrap()
            }
            res.staticFields.forEach { (_, elem) ->
                clazz.defineField(elem)
                    .orReturnThrow { return@f it }
            }
            clazz.toNormal()
        }
    }

    @EsSpec("ConstructorMethod")
    val constructor by lazy {
        elements.find { it.kind == ClassElementKind.CONSTRUCTOR } as ConstructorNode?
    }
}

private class ClassElementCollectResult(
    val privateStaticMethods: MutableMap<PrivateName, PrivateProperty> = mutableMapOf(),
    val privateInstanceMethods: MutableMap<PrivateName, PrivateProperty> = mutableMapOf(),
    val instanceFields: MutableMap<PropertyKey, ClassFieldDefinition> = mutableMapOf(),
    val staticFields: MutableMap<PropertyKey, ClassFieldDefinition> = mutableMapOf(),
): AbstractType
private fun ClassType.collect(elements: List<NormalClassElementNode>, res: ClassElementCollectResult) = lazyFlow f@ {
    val clazz = this@collect
    for (elemNode in elements) {
        if (elemNode.kind == ClassElementKind.CONSTRUCTOR) continue
        val elem = yieldAll(
            if (elemNode.isStatic) evaluateClassElement(elemNode, clazz)
            else evaluateClassElement(elemNode, instancePrototype)
        )
            .orReturn { return@f it }
        when (elem) {
            is PrivateProperty -> {
                val container =
                    if (elemNode.isStatic) res.privateStaticMethods
                    else res.privateInstanceMethods
                val elemInContainer = container[elem.key]
                if (elemInContainer == null) container[elem.key] = elem
                else {
                    require(elem.data is AccessorProperty)
                    require(elemInContainer.data is AccessorProperty)
                    elemInContainer.data.apply {
                        get = get ?: elem.data.get
                        set = set ?: elem.data.set
                    }
                }
            }
            is ClassFieldDefinition -> {
                (
                    if (elemNode.isStatic) res.staticFields
                    else res.instanceFields
                )[elem.name] = elem
            }
            null -> {}
        }
    }
    empty
}

private fun evaluateClassElement(element: ClassElementNode, obj: ObjectType): SimpleLazyFlow<MaybeAbrupt<ClassElementEvaluationResult?>> =
    when (element) {
        is FieldNode -> evaluateField(element, obj)
        is MethodNode -> evaluateMethodDefinition(element, obj)
        is EmptyStatementNode -> lazyFlow { empty }
    }

private fun evaluateField(field: FieldNode, obj: ObjectType) = lazyFlow f@ {
    val name = yieldAll(field.name.toLanguageTypePropertyKey())
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
        )
            .apply {
                homeObject = obj
            }
    }
    ClassFieldDefinition(name, initializer)
        .toWideNormal()
}
