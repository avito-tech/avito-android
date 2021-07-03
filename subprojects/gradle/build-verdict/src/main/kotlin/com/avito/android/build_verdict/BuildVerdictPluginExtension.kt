package com.avito.android.build_verdict

import com.avito.android.build_verdict.span.SpannedString
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty

public abstract class BuildVerdictPluginExtension(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    internal val taskVerdictProviders = objects.listProperty<UserDefinedTaskVerdictProducer>()

    public val outputDir: Property<Directory> =
        objects.directoryProperty().convention(layout.projectDirectory.dir("outputs/build-verdict"))

    public fun onTaskFailure(name: String, verdictProducer: TaskVerdictProducer) {
        taskVerdictProviders.add(
            UserDefinedTaskVerdictProducer(
                predicate = TaskPredicate.ByName(name),
                producer = verdictProducer
            )
        )
    }

    public fun onTaskFailure(acceptedType: Class<in Task>, verdictProducer: TaskVerdictProducer) {
        taskVerdictProviders.add(
            UserDefinedTaskVerdictProducer(
                predicate = TaskPredicate.ByType(acceptedType),
                producer = verdictProducer
            )
        )
    }

    public fun onTaskFailure(name: String, producer: (Task) -> SpannedString) {
        onTaskFailure(name, TaskVerdictProducer.create(producer))
    }

    public fun onTaskFailure(acceptedClass: Class<in Task>, producer: (Task) -> SpannedString) {
        onTaskFailure(acceptedClass, TaskVerdictProducer.create(producer))
    }
}
