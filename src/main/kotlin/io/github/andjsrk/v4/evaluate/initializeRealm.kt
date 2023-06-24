package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("InitializeHostDefinedRealm")
fun initializeRealm() {
    val realm = Realm()
    val newContext = ExecutionContext(ModuleEnvironment(null), realm)
    executionContextStack.push(newContext)
    realm.setGlobalObject(null)
    realm.setDefaultGlobalBindings()
    newContext.lexicalEnvironment = ModuleEnvironment(GlobalEnvironment(realm.globalObject))
}
