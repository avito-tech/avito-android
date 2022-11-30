package com.avito.android.tech_budget

import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter.Companion.DEFAULT_SEPARATOR
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsBatcher
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class CollectWarningsConfiguration @Inject constructor(
    projectLayout: ProjectLayout,
    objects: ObjectFactory,
) {

    public val outputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir("warnings"))

    /**
     * We need a separator to divide one warning to another when saving them.
     *
     * Warning can be multi-line, so line break character shouldn't be used as a separator.
     */
    public val warningsSeparator: Property<String> = objects.property<String>()
        .convention(DEFAULT_SEPARATOR)

    /**
     * Names of tasks which will be used to compile warnings.
     *
     * We don't need to check all build variants because it will produce duplicates
     * and will make huge amount of unnecessary work.
     *
     * Thus, we can define only tasks that are needed.
     */
    public val compileWarningsTaskNames: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(setOf("compileReleaseKotlin"))

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
}
