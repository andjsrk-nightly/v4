package io.github.andjsrk.v4.evaluate

enum class AsyncGeneratorState {
    SUSPENDED_START,
    SUSPENDED_YIELD,
    AWAITING_RETURN,
    EXECUTING,
    COMPLETED,
}
