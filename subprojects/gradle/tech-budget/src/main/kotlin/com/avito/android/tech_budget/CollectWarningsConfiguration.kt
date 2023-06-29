package com.avito.android.tech_budget

import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsBatcher
import com.avito.android.tech_budget.parser.FileParser
import com.avito.android.tech_budget.warnings.CollectWarningsTask
import com.avito.android.tech_budget.warnings.CompilerIssue
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class CollectWarningsConfiguration @Inject constructor(
    objects: ObjectFactory,
) {

    /**
     * Maximum warnings count which will be uploaded in one portion to a server.
     *
     * On a large projects, warnings need to be divided into a batches because project can contain great amount of them,
     * and it's better to upload them in parallel, not in a single chunk.
     */
    public val uploadWarningsBatchSize: Property<Int> = objects.property(Int::class.java)
        .convention(UploadWarningsBatcher.DEFAULT_BATCH_SIZE)

    /**
     * Maximum number of parallel requests to uploadWarnings endpoint when we send warnings in batches.
     */
    public val uploadWarningsParallelRequestsCount: Property<Int> = objects.property(Int::class.java)
        .convention(UploadWarningsBatcher.DEFAULT_PARALLEL_REQUESTS_COUNT)

    /**
     * Used to deserialize warnings output from [CollectWarningsTask].
     *
     * Output can be collected in any format, but then must be transformed to a [CompilerIssue] model with this parser.
     */
    public val issuesFileParser: Property<FileParser<CompilerIssue>> = objects.property()

    /**
     * Names of tasks which will be used to compile warnings.
     *
     * We don't need to check all build variants because it will produce duplicates
     * and will make huge amount of unnecessary work.
     *
     * Thus, we can define only tasks that are needed.
     */
    @Deprecated("Unused anymore, migrated to reportOutputProvider and fileReportParser configurations")
    public val compileWarningsTaskNames: SetProperty<String> = objects.setProperty(String::class.java)

    /**
     * Name of task which will be used to collect warnings
     *
     * Task should be inherited from [CollectWarningsTask] and declare an output file with report.
     */
    public val compileWarningsTaskName: Property<String> = objects.property()
}
