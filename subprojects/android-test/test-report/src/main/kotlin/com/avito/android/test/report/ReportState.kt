package com.avito.android.test.report

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.report.model.Entry
import com.avito.report.model.Incident
import com.avito.report.model.Video

sealed class ReportState {

    abstract val isFirstStepOrPrecondition: Boolean

    object Nothing : ReportState() {
        override val isFirstStepOrPrecondition: Boolean = true
    }

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
            var testCaseStepList: MutableList<StepResult> = mutableListOf()
        ) : Initialized() {

            override val isFirstStepOrPrecondition: Boolean
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

            internal fun addEarlyEntries(entries: List<Entry>) {
                val firstPreconditionOrStep =
                    preconditionStepList.firstOrNull() ?: testCaseStepList.firstOrNull() ?: return

                firstPreconditionOrStep.entryList.addAll(entries)
            }

            internal fun sortStepEntries() {
                preconditionStepList.forEach {
                    it.entryList = it.entryList
                        .sortedBy { it.timeInSeconds }
                        .distinctCounted()
                        .toMutableList()
                }
                testCaseStepList.forEach {
                    it.entryList = it.entryList
                        .sortedBy { it.timeInSeconds }
                        .distinctCounted()
                        .toMutableList()
                }
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

        data class NotStarted(
            override val testMetadata: TestMetadata,
            override var incident: Incident? = null
        ) : Initialized() {
            override val isFirstStepOrPrecondition: Boolean = true
        }
    }

    object Written : ReportState() {
        override val isFirstStepOrPrecondition: Boolean = false
    }
}
