package com.avito.android.build_verdict

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty

@Suppress("UnstableApiUsage")
abstract class BuildVerdictPluginExtension(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val outputDir: Property<Directory> =
        objects.directoryProperty().convention(layout.projectDirectory.dir("outputs/build-verdict"))

    internal val taskVerdictProviders = objects.listProperty<UserDefinedTaskVerdictProducer>()

    fun onTaskFailure(name: String, verdictProducer: TaskVerdictProducer) {
        taskVerdictProviders.add(
            UserDefinedTaskVerdictProducer(
                predicate = TaskPredicate.ByName(name),
                producer = verdictProducer
            )
        )
    }

    fun onTaskFailure(acceptedType: Class<in Task>, verdictProducer: TaskVerdictProducer) {
        taskVerdictProviders.add(
            UserDefinedTaskVerdictProducer(
                predicate = TaskPredicate.ByType(acceptedType),
                producer = verdictProducer
            )
        )
    }

    fun onTaskFailure(name: String, producer: (Task) -> String) {
        onTaskFailure(name, TaskVerdictProducer.create(producer))
    }

    fun onTaskFailure(acceptedClass: Class<in Task>, producer: (Task) -> String) {
        onTaskFailure(acceptedClass, TaskVerdictProducer.create(producer))
    }
}
