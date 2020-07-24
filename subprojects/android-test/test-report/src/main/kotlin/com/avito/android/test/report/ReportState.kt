package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.report.model.Incident
import com.avito.report.model.Video

sealed class ReportState {

    object Nothing : ReportState()

    sealed class Initialized : ReportState() {
        abstract val testMetadata: TestMetadata
        abstract var incident: Incident?

        data class Started(
            override val testMetadata: TestMetadata,
            override var incident: Incident? = null,
            var currentStep: StepResult? = null,
            var stepNumber: Int = 0,
            var preconditionNumber: Int = 0,
            var video: Video? = null,
            var dataSet: DataSet? = null,
            var startTime: Long,
            var endTime: Long = 0,
            var preconditionStepList: MutableList<StepResult> = mutableListOf(),
            var testCaseStepList: MutableList<StepResult> = mutableListOf(),
            var performanceJson: String? = null
        ) : Initialized()

        data class WaitingToStart(
            override val testMetadata: TestMetadata,
            override var incident: Incident? = null
        ) : Initialized()
    }

    object Written : ReportState()
}
