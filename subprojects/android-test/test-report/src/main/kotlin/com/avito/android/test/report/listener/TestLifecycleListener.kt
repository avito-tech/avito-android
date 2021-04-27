package com.avito.android.test.report.listener

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.report.model.Incident

interface TestLifecycleListener {

    fun beforeTestStart(state: Started) {
        // do nothing
    }

    fun beforeTestFinished(state: Started) {
        // do nothing
    }

    fun afterTestFinished(state: Started) {
        // do nothing
    }

    fun afterIncident(incident: Incident) {
        // do nothing
    }
}

object TestLifecycleNotifier : TestLifecycleListener {
    private val listeners = mutableListOf<TestLifecycleListener>()

    fun addListener(listener: TestLifecycleListener) = listeners.add(listener)

    override fun beforeTestStart(state: Started) {
        listeners.forEach { it.beforeTestStart(state) }
    }

    override fun beforeTestFinished(state: Started) {
        listeners.forEach { it.beforeTestFinished(state) }
    }

    override fun afterTestFinished(state: Started) {
        listeners.forEach { it.afterTestFinished(state) }
    }

    override fun afterIncident(incident: Incident) {
        listeners.forEach { it.afterIncident(incident) }
    }
}
