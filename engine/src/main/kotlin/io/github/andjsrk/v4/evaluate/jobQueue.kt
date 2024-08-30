package io.github.andjsrk.v4.evaluate

import io.github.andjsrk.v4.evaluate.type.PromiseType

val jobQueue = ArrayDeque<PromiseType.Reaction.Job>()

fun runJobs() {
    if (getActiveModule() != null) return

    while (jobQueue.isNotEmpty()) {
        val job = jobQueue.removeFirst()
        val newCtx = ExecutionContext(job.realm ?: runningExecutionContext.realm, module = job.module)
        executionContextStack.addTop(newCtx)
        job.closure()
        executionContextStack.removeTop()
    }
    jobQueue.clear()
}
