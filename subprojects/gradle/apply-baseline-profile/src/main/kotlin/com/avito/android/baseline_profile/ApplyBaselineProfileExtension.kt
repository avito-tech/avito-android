package com.avito.android.baseline_profile

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.property

public abstract class ApplyBaselineProfileExtension(
    objects: ObjectFactory,
) {
    public val taskName: Property<String> = objects.property()

    public val instrumentationTaskName: Property<String> = objects.property()

    public val applicationModuleName: Property<String> = objects.property()

    public val applicationVariantName: Property<String> = objects.property()

    public val macrobenchmarksOutputDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Nested
    public abstract val saveToVersionControl: SaveProfileToVersionControlExtension

    public fun saveToVersionControl(action: Action<SaveProfileToVersionControlExtension>) {
        action.execute(saveToVersionControl)
        saveToVersionControl.finalizeValues()
    }

    private fun properties() = listOf(
        taskName,
        instrumentationTaskName,
        applicationModuleName,
        applicationVariantName,
        macrobenchmarksOutputDirectory,
    )

    internal fun validateValues(): ApplyBaselineProfileExtension {
        properties()
            .forEach {
                it.finalizeValue()
                require(it.isPresent) { "Property must be set - $it" }
            }
        return this
    }
}

public abstract class SaveProfileToVersionControlExtension(
    objects: ObjectFactory,
) {
    public val enable: Property<Boolean> = objects.property<Boolean>().convention(false)

    public val enableRemoteOperations: Property<Boolean> = objects.property<Boolean>().convention(true)

    public val commitMessage: Property<String> = objects.property()

    public val includeDetailsInCommitMessage: Property<Boolean> = objects.property<Boolean>().convention(true)

    internal fun finalizeValues() {
        enable.finalizeValue()

        commitMessage.finalizeValue()
        require(commitMessage.isPresent) { "Property must be set - $commitMessage" }
    }
}
