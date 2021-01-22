package com.avito.android

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import javax.inject.Inject

abstract class RobolectricPrefetchTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val prefetchDependencies = objects.mapProperty<String, String>()

    @TaskAction
    fun execute() {
        val configurationsToPrefetch = prefetchDependencies.get().keys
        val robolectricConfigurations = project.configurations.matching { it.name in configurationsToPrefetch }
        robolectricConfigurations.forEach {
            if (it.state != Configuration.State.RESOLVED) {
                it.resolve()
            }
        }
    }
}
