package com.avito.android.tech_budget.warnings

import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles

public interface CollectWarningsTask : Task {

    @get:OutputFile
    public val warnings: RegularFileProperty

    @get:OutputFiles
    public val warningsReports: ConfigurableFileCollection
}
