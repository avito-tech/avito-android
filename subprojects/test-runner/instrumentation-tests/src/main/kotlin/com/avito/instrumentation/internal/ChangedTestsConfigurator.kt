package com.avito.instrumentation.internal

import com.avito.android.InstrumentationChangedTestsFinderApi
import com.avito.android.changedTestsFinderTaskProvider
import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.kotlin.dsl.dependencyOn
import com.avito.runner.scheduler.suite.filter.ChangedTestsFilterMode
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer

internal class ChangedTestsConfigurator(
    private val pluginContainer: PluginContainer,
    private val taskContainer: TaskContainer,
    private val configuration: InstrumentationConfiguration,
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        configuration.changedTestsFilterMode.disallowChanges()
        task.changedTestsFilterMode.set(configuration.changedTestsFilterMode)

        when (val mode = requireNotNull(configuration.changedTestsFilterMode.get())) {
            ChangedTestsFilterMode.ALL -> {
                // empty
            }

            ChangedTestsFilterMode.CHANGED,
            ChangedTestsFilterMode.ALL_EXCEPT_CHANGED -> {
                require(pluginContainer.hasPlugin(InstrumentationChangedTestsFinderApi.pluginId)) {
                    "Can't configure impact mode $mode without plugin ${InstrumentationChangedTestsFinderApi.pluginId}"
                }

                val changedTestsTaskProvider = taskContainer.changedTestsFinderTaskProvider()

                // todo why implicit dependency not working?
                // todo it's hard to write a test because it's different plugins, maybe merge?
                task.dependencyOn(changedTestsTaskProvider) {
                    task.changedTests.set(it.changedTestsFile)
                }
            }
        }
    }
}
