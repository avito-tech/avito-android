package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.createStubInstance
import com.avito.report.model.Incident
import com.avito.report.model.Video

internal fun ReportState.Initialized.Started.Companion.createStubInstance(
    testMetadata: TestMetadata = TestMetadata.createStubInstance(),
    incident: Incident? = null,
    currentStep: StepResult? = null,
    stepNumber: Int = 0,
    preconditionNumber: Int = 0,
    video: Video? = null,
    dataSet: DataSet? = null,
    startTime: Long = 0,
    endTime: Long = 0,
    preconditionStepList: MutableList<StepResult> = mutableListOf(),
    testCaseStepList: MutableList<StepResult> = mutableListOf()
) = ReportState.Initialized.Started(
    testMetadata = testMetadata,
    incident = incident,
    currentStep = currentStep,
    stepNumber = stepNumber,
    preconditionNumber = preconditionNumber,
    video = video,
    dataSet = dataSet,
    startTime = startTime,
    endTime = endTime,
    preconditionStepList = preconditionStepList,
    testCaseStepList = testCaseStepList
)
