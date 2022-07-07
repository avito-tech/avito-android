package com.avito.android.test.report.listener

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.report.model.Incident

public interface TestLifecycleListener {

    public fun beforeTestStart(state: Started) {
        // do nothing
    }

    public fun beforeTestFinished(state: Started) {
        // do nothing
    }

    public fun afterTestFinished(state: Started) {
        // do nothing
    }

    public fun afterIncident(incident: Incident) {
        // do nothing
    }
}

public object TestLifecycleNotifier : TestLifecycleListener {
    private val listeners = mutableListOf<TestLifecycleListener>()

    public fun addListener(listener: TestLifecycleListener) {
        listeners.add(listener)
    }

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
