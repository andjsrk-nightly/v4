package io.github.andjsrk.v4.evaluate.builtin.error

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.requireToBe
import io.github.andjsrk.v4.evaluate.type.AccessorProperty
import io.github.andjsrk.v4.evaluate.type.Completion
import io.github.andjsrk.v4.evaluate.type.lang.NullType
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType
import io.github.andjsrk.v4.evaluate.type.lang.PropertyKey

/**
 * Note that `Error.prototype.name` has been changed to a getter that returns the object's class' name.
 */
@EsSpec("Error.prototype.name")
val nameGetter = AccessorProperty.builtinGetter("name") fn@ {
    val error = it.requireToBe<ObjectType> { return@fn it }
    Completion.Normal(
        error.findName() ?: NullType
    )
}

private tailrec fun ObjectType.findName(): PropertyKey? {
    val proto = prototype ?: return null
    return proto.ownerClass.name ?: proto.findName()
}
