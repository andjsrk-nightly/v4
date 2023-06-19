package io.github.andjsrk.v4.evaluate.type

import io.github.andjsrk.v4.EsSpec
import io.github.andjsrk.v4.evaluate.type.lang.ObjectType

class Realm: Record {
    init {
        createIntrinsics()
    }
    lateinit var global: ObjectType
    lateinit var globalEnv: GlobalEnvironment
    @EsSpec("SetRealmGlobalObject")
    @JvmName("setGlobalObjectNonProperty")
    fun setGlobalObject(baseGlobal: ObjectType?) {
        global = baseGlobal ?: ObjectType.create(null)
        globalEnv = GlobalEnvironment(global)
    }
    @EsSpec("SetDefaultGlobalBindings")
    fun setDefaultGlobalBindings() {
        for ((key, desc) in io.github.andjsrk.v4.evaluate.builtin.global.properties) {
            global.definePropertyOrThrow(key, desc)
        }
    }
    fun createIntrinsics() {

    }
}
