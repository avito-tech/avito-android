package com.avito.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@Deprecated("Modules visibility restriction is deprecated. Left for backward compatibility")
public abstract class CheckProjectDependenciesOwnershipTask : DefaultTask() {

    @TaskAction
    public fun checkProjectRelations() {
        // no-op
    }
}
