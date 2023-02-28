package com.avito.android.tech_budget.internal.lint_issues.collect

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.OutputFiles

internal abstract class CollectLintIssuesTask : DefaultTask() {
    @get:OutputFiles
    abstract val outputXmlFiles: ConfigurableFileCollection

    companion object {
        const val NAME = "collectLintIssues"
    }
}
