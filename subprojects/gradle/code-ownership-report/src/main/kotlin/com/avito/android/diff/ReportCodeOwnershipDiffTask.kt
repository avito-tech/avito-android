package com.avito.android.diff

import com.avito.android.diff.comparator.EqualsOwnersComparator
import com.avito.android.diff.comparator.OwnersComparator
import com.avito.android.diff.counter.OwnersDiffCounter
import com.avito.android.diff.counter.OwnersDiffCounterImpl
import com.avito.android.diff.extractor.OwnersExtractor
import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.formatter.SimpleOwnersDiffMessageFormatter
import com.avito.android.diff.report.OwnersDiffReportDestination
import com.avito.android.diff.report.OwnersDiffReporter
import com.avito.android.diff.report.OwnersDiffReporterFactory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

public abstract class ReportCodeOwnershipDiffTask : DefaultTask() {

    @get:Input
    public abstract val expectedOwnersExtractor: Property<OwnersExtractor>

    @get:Input
    public abstract val actualOwnersExtractor: Property<OwnersExtractor>

    @get:Input
    public abstract val diffReportDestination: Property<OwnersDiffReportDestination>

    @get:Input
    @get:Optional
    public abstract val comparator: Property<OwnersComparator>

    @get:Input
    @get:Optional
    public abstract val messageFormatter: Property<OwnersDiffMessageFormatter>

    @TaskAction
    public fun doWork() {
        makeOwnershipReport(
            expectedOwnersExtractor = expectedOwnersExtractor.get(),
            actualOwnersExtractor = actualOwnersExtractor.get(),
            diffCounter = OwnersDiffCounterImpl(comparator.orNull ?: EqualsOwnersComparator()),
            diffReporter = OwnersDiffReporterFactory(extractMessageFormatter()).create(diffReportDestination.get())
        )
    }

    private fun makeOwnershipReport(
        expectedOwnersExtractor: OwnersExtractor,
        actualOwnersExtractor: OwnersExtractor,
        diffCounter: OwnersDiffCounter,
        diffReporter: OwnersDiffReporter
    ) {
        val expectedOwners = expectedOwnersExtractor.extractOwners()
        val actualOwners = actualOwnersExtractor.extractOwners()
        val diff = diffCounter.countOwnersDiff(expectedOwners, actualOwners)
        diffReporter.reportDiffFound(diff)
    }

    private fun extractMessageFormatter(): OwnersDiffMessageFormatter =
        messageFormatter.orNull ?: SimpleOwnersDiffMessageFormatter()
}
