package com.avito.android.tech_budget.perf_screen_owners

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

public interface CollectPerfOwnersTask : Task {

    @get:OutputFile
    public val perfOwnersOutput: RegularFileProperty
}
