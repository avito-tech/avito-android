package com.avito.android.test.report

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class InfrastructureTestRule(
    private val assertionBlock: (ReportState.Initialized.Started) -> Unit
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            try {
                base.evaluate()
            } finally {
                assertionBlock(
                    getCurrentReportState()
                )
            }
        }
    }

    private fun getCurrentReportState(): ReportState.Initialized.Started {
        val currentReport = reportInstance
        if (currentReport !is ReportStateProvider) {
            throw RuntimeException("Report must implement ReportStateProvider for integration testing")
        }

        val currentState = currentReport.getCurrentState()
        if (currentState !is ReportState.Initialized.Started) {
            throw RuntimeException("Report state must be Initialized.Started during test execution")
        }

        return currentState
    }
}
