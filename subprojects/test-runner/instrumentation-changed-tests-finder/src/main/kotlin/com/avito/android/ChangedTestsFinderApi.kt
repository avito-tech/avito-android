package com.avito.android

import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderTaskName
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

fun TaskContainer.changedTestsFinderTaskProvider() = typedNamed<FindChangedTestsTask>(changedTestsFinderTaskName)

object InstrumentationChangedTestsFinderApi {

    internal const val changedTestsFinderTaskName = "changedTestsFinderTask"

    internal const val changedTestsFinderExtensionName = "changedTests"

    const val pluginId = "com.avito.android.instrumentation-changed-tests-finder"
}
