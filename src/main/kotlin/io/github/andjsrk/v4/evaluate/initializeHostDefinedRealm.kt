package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.ModuleEnvironment
import io.github.andjsrk.v4.evaluate.type.Realm
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

@EsSpec("InitializeHostDefinedRealm")
fun initializeHostDefinedRealm(baseGlobal: ObjectType? = null) {
    val realm = Realm()
    val newContext = ExecutionContext(ModuleEnvironment(), realm)
    Evaluator.runningExecutionContext = newContext
    realm.setGlobalObject(baseGlobal)
    realm.setDefaultGlobalBindings()
}
