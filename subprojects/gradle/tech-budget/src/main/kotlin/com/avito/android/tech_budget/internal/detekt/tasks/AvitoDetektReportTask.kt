package com.avito.android.tech_budget.internal.detekt.tasks

import com.avito.android.tech_budget.warnings.CollectWarningsTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

@CacheableTask
public abstract class AvitoDetektReportTask : DefaultTask(), CollectWarningsTask {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val reports: ConfigurableFileCollection

    override val warningsReports: ConfigurableFileCollection
        get() = reports
}
