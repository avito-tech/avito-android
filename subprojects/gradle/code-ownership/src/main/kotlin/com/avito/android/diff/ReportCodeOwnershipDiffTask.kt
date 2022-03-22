package com.avito.android.diff

import com.avito.android.diff.comparator.OwnersComparator
import com.avito.android.diff.counter.OwnersDiffCounter
import com.avito.android.diff.counter.OwnersDiffCounterImpl
import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.provider.OwnersProvider
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
    public abstract val expectedOwnersProvider: Property<OwnersProvider>

    @get:Input
    public abstract val actualOwnersProvider: Property<OwnersProvider>

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
            expectedOwnersProvider = expectedOwnersProvider.get(),
            actualOwnersProvider = actualOwnersProvider.get(),
            diffCounter = OwnersDiffCounterImpl(comparator.get()),
            diffReporter = OwnersDiffReporterFactory(messageFormatter.get()).create(diffReportDestination.get())
        )
    }

    private fun makeOwnershipReport(
        expectedOwnersProvider: OwnersProvider,
        actualOwnersProvider: OwnersProvider,
        diffCounter: OwnersDiffCounter,
        diffReporter: OwnersDiffReporter
    ) {
        val expectedOwners = expectedOwnersProvider.get()
        val actualOwners = actualOwnersProvider.get()
        val diff = diffCounter.countOwnersDiff(expectedOwners, actualOwners)
        diffReporter.reportDiffFound(diff)
    }
}
