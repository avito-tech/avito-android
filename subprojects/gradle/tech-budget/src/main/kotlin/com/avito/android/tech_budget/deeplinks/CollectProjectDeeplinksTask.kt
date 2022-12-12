package com.avito.android.tech_budget.deeplinks

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

public interface CollectProjectDeeplinksTask : Task {

    @get:OutputFile
    public val deeplinksOutput: RegularFileProperty
}
