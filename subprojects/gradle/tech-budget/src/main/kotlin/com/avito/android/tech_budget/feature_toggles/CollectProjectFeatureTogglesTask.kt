package com.avito.android.tech_budget.feature_toggles

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

public interface CollectProjectFeatureTogglesTask : Task {

    @get:OutputFile
    public val featureTogglesOutput: RegularFileProperty
}
