package io.github.andjsrk.v4.evaluate.type

class GraphLoadingState(val promiseCapability: PromiseType.Capability): Record {
    var isLoading = true
    var pendingModulesCount = 1
    val visited = mutableListOf<CyclicModule>()
}
