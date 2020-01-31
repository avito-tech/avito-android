package com.avito.android.test.report.listener

import com.avito.android.test.report.model.StepResult

interface StepLifecycleListener {

    fun beforeStepStart(result: StepResult) {
        // do nothing
    }

    fun beforeStepUpdate(result: StepResult) {
        // do nothing
    }

    fun afterStepUpdate(result: StepResult) {
        // do nothing
    }

    fun afterStepStop(result: StepResult) {
        // do nothing
    }
}

object StepLifecycleNotifier : StepLifecycleListener {
    private val listeners = mutableListOf<StepLifecycleListener>()

    fun addListener(listener: StepLifecycleListener) = listeners.add(listener)

    override fun beforeStepStart(result: StepResult) {
        listeners.forEach { it.beforeStepStart(result) }
    }

    override fun beforeStepUpdate(result: StepResult) {
        listeners.forEach { it.beforeStepUpdate(result) }
    }

    override fun afterStepUpdate(result: StepResult) {
        listeners.forEach { it.afterStepUpdate(result) }
    }

    override fun afterStepStop(result: StepResult) {
        listeners.forEach { it.afterStepStop(result) }
    }
}
