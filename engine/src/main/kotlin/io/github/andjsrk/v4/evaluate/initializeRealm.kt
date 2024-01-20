package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.HostConfig
import io.github.andjsrk.v4.evaluate.type.*

@EsSpec("InitializeHostDefinedRealm")
fun initializeRealm() {
    val realm = Realm()
    val newContext = ExecutionContext(realm, ModuleEnvironment(null))
    executionContextStack.addTop(newContext)
    realm.setGlobalObject(null)
    realm.setDefaultGlobalBindings()
    HostConfig.value.applyGlobalProperties(realm.globalObject)
    newContext.lexicalEnvNotNull = ModuleEnvironment(GlobalEnvironment(realm.globalObject))
}
