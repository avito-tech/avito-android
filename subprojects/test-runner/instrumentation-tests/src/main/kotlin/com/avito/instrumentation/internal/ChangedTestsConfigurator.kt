package com.avito.instrumentation.internal

import com.avito.android.InstrumentationChangedTestsFinderApi
import com.avito.android.changedTestsFinderTaskProvider
import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.kotlin.dsl.dependencyOn
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer

internal class ChangedTestsConfigurator(
    private val pluginContainer: PluginContainer,
    private val taskContainer: TaskContainer,
    private val configuration: InstrumentationConfiguration,
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        val runOnlyChangedTests = configuration.runOnlyChangedTests

        task.runOnlyChangedTests.set(runOnlyChangedTests)

        if (runOnlyChangedTests && pluginContainer.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
            val impactTaskProvider = taskContainer.changedTestsFinderTaskProvider()

            // todo why implicit dependency not working?
            // todo it's hard to write a test because it's different plugins, maybe merge?
            task.dependencyOn(impactTaskProvider) {
                task.changedTests.set(it.changedTestsFile)
            }
        }
    }
}
