@file:Suppress("UnstableApiUsage")

package com.avito.module.internal.dependencies

import com.avito.logger.GradleLoggerFactory
import com.avito.module.dependencies.FindAndroidAppTask.Options.CONFIGURATION
import com.avito.module.dependencies.FindAndroidAppTask.Options.CONFIGURATION.android_test
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import javax.inject.Inject

internal abstract class PrintDependenciesGraphTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        description = "Print dependencies graph. Must be used only in gradleTests"
    }

    @get:Option(option = "flatten", description = "Print tree flatten, default is false")
    @get:Input
    val flatten: Property<Boolean> = objects
        .property(Boolean::class.java)
        .convention(false)

    @get:Option(
        option = "configuration",
        description = "Choose in what configuration we will look up, default is android_test"
    )
    @get:Input
    val configuration: Property<CONFIGURATION> = objects
        .property(CONFIGURATION::class.java)
        .convention(android_test)

    @TaskAction
    fun action() {
        if (flatten.getOrElse(false)) {
            printDependenciesGraphFlatten()
        } else {
            printDependenciesGraph()
        }
        logger.lifecycle("print ended")
    }

    private fun printDependenciesGraph() {
        DependenciesGraphBuilder(project, GradleLoggerFactory.fromTask(this))
            .buildDependenciesGraph()
            .forEach { node ->
                node.print("Root ")
            }
    }

    private fun printDependenciesGraphFlatten() {
        DependenciesGraphBuilder(project, GradleLoggerFactory.fromTask(this))
            .buildDependenciesGraphFlatten(configuration.getOrElse(android_test).mapToType())
            .forEach { (project, deps) ->
                logger.lifecycle("Root ${project.path}")
                deps.forEach { dependencyProject ->
                    logger.lifecycle("${project.path} graph contains ${dependencyProject.path}")
                }
            }
    }

    private fun ModuleProjectDependenciesNode.print(prefix: String = "") {
        logger.lifecycle("${prefix}Node ${project.path}")
        dependencies
            .mapValues { (_, nodeSet) -> nodeSet.map { it.project.path } }
            .forEach { conf, projects ->
                projects.forEach { project ->
                    logger.lifecycle("${this.project.path} depends on $project in ${conf.type}")
                }
            }
        dependencies.values.flatten().toSet().forEach { node ->
            node.print("${project.path} ")
        }
    }
}
