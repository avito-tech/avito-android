package com.avito.android.test.report.listener

import com.avito.android.test.report.model.StepResult

interface PreconditionLifecycleListener {

    fun beforePreconditionStart(result: StepResult) {
        // do nothing
    }

    fun beforePreconditionUpdate(result: StepResult) {
        // do nothing
    }

    fun afterPreconditionUpdate(result: StepResult) {
        // do nothing
    }

    fun afterPreconditionStop(result: StepResult) {
        // do nothing
    }
}

object PreconditionLifecycleNotifier : PreconditionLifecycleListener {
    private val listeners = mutableListOf<PreconditionLifecycleListener>()

    fun addListener(listener: PreconditionLifecycleListener) = listeners.add(listener)

    override fun beforePreconditionStart(result: StepResult) {
        listeners.forEach { it.beforePreconditionStart(result) }
    }

    override fun beforePreconditionUpdate(result: StepResult) {
        listeners.forEach { it.beforePreconditionUpdate(result) }
    }

    override fun afterPreconditionUpdate(result: StepResult) {
        listeners.forEach { it.afterPreconditionUpdate(result) }
    }

    override fun afterPreconditionStop(result: StepResult) {
        listeners.forEach { it.afterPreconditionStop(result) }
    }
}
