package com.avito.module.dependencies

import com.avito.module.configurations.ConfigurationType
import com.avito.module.configurations.ConfigurationType.AndroidTests
import com.avito.module.configurations.ConfigurationType.Main
import com.avito.module.dependencies.FindAndroidAppTask.Options.CONFIGURATION
import com.avito.module.internal.dependencies.AndroidAppsGraphBuilder
import com.avito.module.internal.dependencies.DependenciesGraphBuilder
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction
import com.avito.module.internal.dependencies.FindAndroidAppTaskAdvisor
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.util.Path
import javax.inject.Inject

public abstract class FindAndroidAppTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        description = "Task that finds the closest Android app by modules your need"
    }

    @get:Option(
        option = "configuration",
        description = "Choose in what configuration we will look up, default is android_test"
    )
    @get:Input
    public val configuration: Property<CONFIGURATION> =
        objects.property(CONFIGURATION::class.java).convention(CONFIGURATION.android_test)

    @get:Option(
        option = "modules",
        description = "Modules for which we will look for Android app." +
            " If you want to search for list of modules split them by coma ','"
    )
    @get:Input
    public abstract val modules: Property<String>

    @TaskAction
    public fun action() {
        val modules = parseModules()
        val inputConfiguration = configuration.get()
        val graphBuilder = DependenciesGraphBuilder(project.rootProject)
        val androidAppsGraphBuilder = AndroidAppsGraphBuilder(graphBuilder)
        val action = FindAndroidAppTaskAction(androidAppsGraphBuilder)
        val advisor = FindAndroidAppTaskAdvisor()
        val verdict = action.findAppFor(modules, inputConfiguration.mapToTypes())
        logger.lifecycle(advisor.giveAdvice(verdict))
    }

    private fun parseModules(): Set<Path> {
        return modules
            .map {
                it.split(',').map { module ->
                    val path = Path.path(module)
                    require(path.isAbsolute) {
                        "module '$module' must contain ${Path.SEPARATOR} and be absolute"
                    }
                    requireNotNull(project.rootProject.findProject(path.path)) {
                        "module '$path' does not exist"
                    }
                    path
                }.toSet()
            }.get()
    }

    public object Options {

        @Suppress("EnumEntryName")
        public enum class CONFIGURATION {
            main {
                override fun mapToTypes(): Set<ConfigurationType> = setOf(Main)
            },
            android_test {
                override fun mapToTypes(): Set<ConfigurationType> = setOf(AndroidTests, Main)
            };

            public abstract fun mapToTypes(): Set<ConfigurationType>
        }
    }
}
