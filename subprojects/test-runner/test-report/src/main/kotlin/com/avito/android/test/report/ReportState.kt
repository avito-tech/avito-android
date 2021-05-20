package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepAttachments
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.filestorage.FutureValue
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.Video
import java.util.concurrent.CopyOnWriteArrayList

sealed class ReportState {

    sealed class NotFinished : ReportState() {

        object NotInitialized : NotFinished()

        sealed class Initialized : NotFinished() {
            abstract val testMetadata: TestMetadata
            abstract var incident: Incident?
            abstract var incidentScreenshot: FutureValue<Entry.File>?
            abstract val attachmentsBeforeSteps: StepAttachments

            class NotStarted(
                override val attachmentsBeforeSteps: StepAttachments = StepAttachments(),
                override val testMetadata: TestMetadata,
                override var incident: Incident? = null,
                override var incidentScreenshot: FutureValue<Entry.File>? = null
            ) : Initialized()

            /**
             * This state is modified concurrently i.e.
             * [preconditionStepList] and [testCaseStepList] are modified from StepsDsl and when creating synthetic step
             * All collections must be thread-safe
             */
            class Started(
                override val attachmentsBeforeSteps: StepAttachments = StepAttachments(),
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
                var preconditionStepList: CopyOnWriteArrayList<StepResult> = CopyOnWriteArrayList(),
                var testCaseStepList: CopyOnWriteArrayList<StepResult> = CopyOnWriteArrayList()
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
                        CopyOnWriteArrayList(
                            testCaseStepList
                                .map { it.appendFutureEntries() }
                        )

                    preconditionStepList =
                        CopyOnWriteArrayList(
                            preconditionStepList
                                .map { it.appendFutureEntries() }
                        )

                    addEarlyEntries()
                    sortStepEntries()
                    incident = incident?.appendScreenshot()
                }

                private fun StepResult.appendFutureEntries(): StepResult {
                    if (attachments.uploads.isEmpty()) return this
                    val newAttachments = StepAttachments()
                    newAttachments.entries.addAll(attachments.entries)
                    newAttachments.entries.addAll(attachments.uploads.map { it.get() })
                    return copy(attachments = newAttachments)
                }

                private fun Incident.appendScreenshot(): Incident {
                    val screenshot = incidentScreenshot ?: return this
                    return copy(entryList = entryList + screenshot.get())
                }

                private fun addEarlyEntries() {
                    val firstPreconditionOrStep =
                        preconditionStepList.firstOrNull() ?: testCaseStepList.firstOrNull() ?: return
                    with(firstPreconditionOrStep.attachments) {
                        entries.addAll(attachmentsBeforeSteps.entries)
                        entries.addAll(attachmentsBeforeSteps.uploads.map { it.get() })
                    }
                }

                private fun sortStepEntries() {
                    preconditionStepList = CopyOnWriteArrayList(
                        preconditionStepList.map {
                            val newAttachment = StepAttachments()
                            newAttachment.entries.addAll(
                                it.attachments.entries
                                    .sortedBy { entry -> entry.timeInSeconds }
                                    .distinctCounted()
                            )
                            it.copy(
                                attachments = newAttachment
                            )
                        }
                    )

                    testCaseStepList = CopyOnWriteArrayList(
                        testCaseStepList.map {
                            val newAttachment = StepAttachments()
                            newAttachment.entries.addAll(
                                it.attachments.entries
                                    .sortedBy { entry -> entry.timeInSeconds }
                                    .distinctCounted()
                            )
                            it.copy(
                                attachments = newAttachment
                            )
                        }
                    )
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
