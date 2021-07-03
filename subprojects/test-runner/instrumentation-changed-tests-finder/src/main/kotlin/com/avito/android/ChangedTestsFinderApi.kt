package com.avito.android

import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderTaskName
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

public fun TaskContainer.changedTestsFinderTaskProvider(): TaskProvider<FindChangedTestsTask> =
    typedNamed(changedTestsFinderTaskName)

public object InstrumentationChangedTestsFinderApi {

    internal const val changedTestsFinderTaskName = "changedTestsFinderTask"

    internal const val changedTestsFinderExtensionName = "changedTests"

    public const val pluginId: String = "com.avito.android.instrumentation-changed-tests-finder"
}
