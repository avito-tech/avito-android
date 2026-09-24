package com.avito.android.tech_budget.internal.detekt.tasks

import com.avito.android.tech_budget.internal.detekt.csv
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
public abstract class TechBudgetDetektTask @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    workerExecutor: WorkerExecutor,
) : Detekt(objects, workerExecutor, providers) {

    /**
     * Compiled classes of the analyzed Android component, set by AGP.
     * Needed for type resolution of Kotlin code that references Java classes of the same module.
     */
    @get:Classpath
    internal abstract val projectClassesDirectories: ListProperty<Directory>

    @get:Classpath
    internal abstract val projectClassesJars: ListProperty<RegularFile>

    @get:OutputFile
    public val warnings: RegularFileProperty = objects.fileProperty()
        .value(providers.provider { reports.csv.outputLocation.get() })
}
