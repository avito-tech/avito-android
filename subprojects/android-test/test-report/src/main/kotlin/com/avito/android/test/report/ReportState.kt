package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.Video

sealed class ReportState {

    sealed class NotFinished : ReportState() {
        abstract val entriesBeforeSteps: MutableList<Entry>
        abstract val uploadsBeforeSteps: MutableList<FutureValue<Entry.File>>

        data class NotInitialized(
            override val entriesBeforeSteps: MutableList<Entry> = mutableListOf(),
            override val uploadsBeforeSteps: MutableList<FutureValue<Entry.File>> = mutableListOf()
        ) : NotFinished()

        sealed class Initialized : NotFinished() {
            abstract val testMetadata: TestMetadata
            abstract var incident: Incident?
            abstract var incidentScreenshot: FutureValue<Entry.File>?

            data class NotStarted(
                override val entriesBeforeSteps: MutableList<Entry>,
                override val uploadsBeforeSteps: MutableList<FutureValue<Entry.File>>,
                override val testMetadata: TestMetadata,
                override var incident: Incident? = null,
                override var incidentScreenshot: FutureValue<Entry.File>? = null
            ) : Initialized()

            data class Started(
                override val entriesBeforeSteps: MutableList<Entry>,
                override val uploadsBeforeSteps: MutableList<FutureValue<Entry.File>>,
                override val testMetadata: TestMetadata,
                override var incident: Incident? = null,
                override var incidentScreenshot: FutureValue<Entry.File>? = null,
                var currentStep: StepResult? = null,
                var stepNumber: Int = 0,
                var preconditionNumber: Int = 0,
                var video: Video? = null,
                var dataSet: DataSet? = null,
                var startTime: Long,
                var endTime: Long = 0,
                var preconditionStepList: MutableList<StepResult> = mutableListOf(),
                var testCaseStepList: MutableList<StepResult> = mutableListOf()
            ) : Initialized() {

                val isFirstStepOrPrecondition: Boolean
                    get() = preconditionNumber == 1 || preconditionNumber == 0 && stepNumber == 1

                internal fun getCurrentStepOrCreate(factory: () -> StepResult): StepResult {
                    return currentStep ?: factory().also { step ->
                        currentStep = step
                        when {
                            testCaseStepList.isNotEmpty() -> testCaseStepList.add(step)
                            else -> preconditionStepList.add(step)
                        }
                    }
                }

                /**
                 * Screenshots/HttpStatic are synchronous, but uploading runs on background thread
                 * We have to wait upload completion before sending report packages
                 */
                internal fun waitUploads() {
                    testCaseStepList =
                        testCaseStepList
                            .map { it.appendFutureEntries() }
                            .toMutableList()

                    preconditionStepList =
                        preconditionStepList
                            .map { it.appendFutureEntries() }
                            .toMutableList()

                    addEarlyEntries()
                    sortStepEntries()
                    incident = incident?.appendScreenshot()
                }

                private fun StepResult.appendFutureEntries(): StepResult {
                    if (futureUploads.isEmpty()) return this
                    return copy(entryList = (entryList + futureUploads.map { it.get() }).toMutableList())
                }

                private fun Incident.appendScreenshot(): Incident {
                    val screenshot = incidentScreenshot ?: return this
                    return copy(entryList = entryList + screenshot.get())
                }

                private fun addEarlyEntries() {
                    val firstPreconditionOrStep =
                        preconditionStepList.firstOrNull() ?: testCaseStepList.firstOrNull() ?: return
                    entriesBeforeSteps.addAll(uploadsBeforeSteps.map { it.get() })
                    firstPreconditionOrStep.entryList.addAll(entriesBeforeSteps)
                }

                private fun sortStepEntries() {
                    preconditionStepList = preconditionStepList.map {
                        it.copy(
                            entryList = it.entryList
                                .sortedBy { it.timeInSeconds }
                                .distinctCounted()
                                .toMutableList()
                        )
                    }.toMutableList()

                    testCaseStepList = testCaseStepList.map {
                        it.copy(
                            entryList = it.entryList
                                .sortedBy { it.timeInSeconds }
                                .distinctCounted()
                                .toMutableList()
                        )
                    }.toMutableList()
                }

                private data class Counted<T>(val t: T, var count: Int = 1)

                /**
                 * сворачивает встречающиеся подряд одинаковые entry в один,
                 * проставляя им в начало лейбла кол-во свернутых элементов
                 */
                @Suppress("UNCHECKED_CAST")
                private inline fun <reified T : Entry> List<T>.distinctCounted(): List<T> =
                    fold(listOf<Counted<T>>()) { acc, entry ->
                        val last = acc.lastOrNull()
                        val lastT = last?.t
                        if (entry is Entry.Comment && lastT is Entry.Comment && entry.title == lastT.title) {
                            acc.apply { this.last().count++ }
                        } else {
                            acc + Counted(entry, 1)
                        }
                    }.map { counted: Counted<T> ->
                        if (counted.t is Entry.Comment && counted.count > 1) {
                            counted.t.copy(title = "[x${counted.count}] ${counted.t.title}")
                        } else {
                            counted.t
                        }
                    } as List<T>

                companion object
            }
        }
    }

    object Finished : ReportState()
}
