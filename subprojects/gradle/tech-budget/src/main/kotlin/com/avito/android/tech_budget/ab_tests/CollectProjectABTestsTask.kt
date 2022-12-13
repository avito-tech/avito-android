package com.avito.android.tech_budget.ab_tests

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

public interface CollectProjectABTestsTask : Task {

    @get:OutputFile
    public val abTestsOutput: RegularFileProperty
}
