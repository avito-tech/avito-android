package com.avito.android.test.report.listener

import com.avito.android.test.report.ReportState
import com.avito.report.model.Incident

interface TestLifecycleListener {

    fun beforeTestStart(state: ReportState.Initialized.Started) {
        //do nothing
    }

    fun beforeTestUpdate(state: ReportState.Initialized.Started) {
        //do nothing
    }

    fun afterTestUpdate(state: ReportState.Initialized.Started) {
        //do nothing
    }

    fun afterTestStop(state: ReportState.Initialized.Started) {
        //do nothing
    }

    fun beforeTestWrite(state: ReportState.Initialized.Started) {
        //do nothing
    }

    fun testWriteError(error: Throwable) {
        //do nothing
    }

    fun afterIncident(incident: Incident) {
        //do nothing
    }

    fun screenshotUploadError(error: Throwable) {
        //do nothing
    }
}

object TestLifecycleNotifier : TestLifecycleListener {
    private val listeners = mutableListOf<TestLifecycleListener>()

    fun addListener(listener: TestLifecycleListener) = listeners.add(listener)

    override fun beforeTestStart(state: ReportState.Initialized.Started) {
        listeners.forEach { it.beforeTestStart(state) }
    }

    override fun beforeTestUpdate(state: ReportState.Initialized.Started) {
        listeners.forEach { it.beforeTestUpdate(state) }
    }

    override fun afterTestUpdate(state: ReportState.Initialized.Started) {
        listeners.forEach { it.afterTestUpdate(state) }
    }

    override fun afterTestStop(state: ReportState.Initialized.Started) {
        listeners.forEach { it.afterTestStop(state) }
    }

    override fun beforeTestWrite(state: ReportState.Initialized.Started) {
        listeners.forEach { it.beforeTestWrite(state) }
    }

    override fun testWriteError(error: Throwable) {
        listeners.forEach { it.testWriteError(error) }
    }

    override fun afterIncident(incident: Incident) {
        listeners.forEach { it.afterIncident(incident) }
    }

    override fun screenshotUploadError(error: Throwable) {
        listeners.forEach { it.screenshotUploadError(error) }
    }
}
