package com.avito.android.runner.report

import com.avito.report.model.AndroidTest

/**
 * Legacy way of interacting with report model; internal ReportViewer service domain leaking here
 */
public interface AvitoReport {

    public fun finish()

    public fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    public companion object
}
