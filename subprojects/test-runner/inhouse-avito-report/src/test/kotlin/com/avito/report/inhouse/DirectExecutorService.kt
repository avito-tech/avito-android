package com.avito.report.inhouse

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

internal class DirectExecutorService : AbstractExecutorService() {
    private var terminated = false

    override fun shutdown() {
        terminated = true
    }

    override fun shutdownNow(): List<Runnable> = emptyList()

    override fun isShutdown(): Boolean = terminated

    override fun isTerminated(): Boolean = terminated

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = terminated

    override fun execute(command: Runnable) {
        command.run()
    }
}
