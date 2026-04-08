package com.avito.android.string_transform.internal.task.signing

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

internal abstract class ValidateSigningIntegrationTask : DefaultTask() {

    @get:Input
    abstract val signServicePluginApplied: Property<Boolean>

    @get:Input
    abstract val failureMessage: Property<String>

    @TaskAction
    fun validate() {
        if (!signServicePluginApplied.get()) {
            throw GradleException(failureMessage.get())
        }
    }
}
