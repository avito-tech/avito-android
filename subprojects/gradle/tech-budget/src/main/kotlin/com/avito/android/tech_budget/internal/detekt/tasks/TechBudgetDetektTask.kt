package com.avito.android.tech_budget.internal.detekt.tasks

import com.avito.android.tech_budget.internal.detekt.csv
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
public abstract class TechBudgetDetektTask @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    workerExecutor: WorkerExecutor,
) : Detekt(objects, workerExecutor, providers) {

    @get:OutputFile
    public val warnings: RegularFileProperty = objects.fileProperty()
        .value(providers.provider { reports.csv.outputLocation.get() })
}
