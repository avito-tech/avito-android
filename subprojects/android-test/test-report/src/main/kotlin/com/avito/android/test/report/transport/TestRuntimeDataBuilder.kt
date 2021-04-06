package com.avito.android.test.report.transport

import com.avito.android.test.report.ReportState
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.time.TimeProvider

internal class TestRuntimeDataBuilder(private val timeProvider: TimeProvider) : PreTransportMappers {

    fun fromState(state: ReportState.Initialized.Started): TestRuntimeDataPackage {
        return try {
            TestRuntimeDataPackage(
                incident = state.incident,
                dataSetData = state.dataSet?.serialize() ?: emptyMap(),
                video = state.video,
                preconditions = transformStepList(state.preconditionStepList),
                steps = transformStepList(state.testCaseStepList),
                startTime = state.startTime,
                endTime = state.endTime
            )
        } catch (e: Throwable) {
            createSafeRuntimeMetadataWithError("Can't build test runtime data", e)
        }
    }

    /**
     * goal is to deliver 'error' status test even in the worst case
     */
    private fun createSafeRuntimeMetadataWithError(errorMessage: String, e: Throwable): TestRuntimeDataPackage {
        val now = timeProvider.nowInSeconds()

        val message = "$errorMessage (${e.message})"

        return TestRuntimeDataPackage(
            incident = Incident(
                type = Incident.Type.INFRASTRUCTURE_ERROR,
                timestamp = now,
                trace = emptyList(),
                chain = listOf(IncidentElement(message = message)),
                entryList = emptyList()
            ),
            startTime = now,
            endTime = now,
            dataSetData = emptyMap(),
            video = null,
            preconditions = emptyList(),
            steps = emptyList()
        )
    }
}
