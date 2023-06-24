package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.builtin.global
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

class Realm: Record {
    init {
        createIntrinsics()
    }
    lateinit var globalObject: ObjectType
    lateinit var globalEnv: GlobalEnvironment
    @EsSpec("SetRealmGlobalObject")
    @JvmName("setGlobalObjectNonProperty")
    fun setGlobalObject(baseGlobal: ObjectType?) {
        globalObject = baseGlobal ?: ObjectType.create(null)
        globalEnv = GlobalEnvironment(globalObject)
    }
    @EsSpec("SetDefaultGlobalBindings")
    fun setDefaultGlobalBindings() {
        for ((key, desc) in global.properties) {
            globalObject.definePropertyOrThrow(key, desc)
        }
    }
    fun createIntrinsics() {

    }
}
