package com.avito.android.test.report

import com.avito.android.test.report.ReportState.NotFinished.Initialized.Started
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepAttachments
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.createStubInstance
import com.avito.report.model.Incident
import com.avito.report.model.Video
import java.util.concurrent.CopyOnWriteArrayList

internal fun Started.Companion.createStubInstance(
    testMetadata: TestMetadata = TestMetadata.createStubInstance(),
    incident: Incident? = null,
    currentStep: StepResult? = null,
    stepNumber: Int = 0,
    preconditionNumber: Int = 0,
    video: Video? = null,
    dataSet: DataSet? = null,
    startTime: Long = 0,
    endTime: Long = 0,
    preconditionStepList: CopyOnWriteArrayList<StepResult> = CopyOnWriteArrayList(),
    testCaseStepList: CopyOnWriteArrayList<StepResult> = CopyOnWriteArrayList(),
    attachmentsBeforeSteps: StepAttachments = StepAttachments(),
) = Started(
    attachmentsBeforeSteps = attachmentsBeforeSteps,
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
