package com.avito.android.baseline_profile.configuration

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class ApplyBaselineProfileConfiguration(
    public val name: String,
) {
    public abstract val instrumentationTaskName: Property<String>

    public abstract val applicationModuleName: Property<String>

    public abstract val applicationVariantName: Property<String>

    public abstract val macrobenchmarksOutputDirectory: DirectoryProperty

    @get:Nested
    public abstract val saveToVersionControl: SaveProfileToVersionControlExtension

    public fun saveToVersionControl(action: Action<SaveProfileToVersionControlExtension>) {
        action.execute(saveToVersionControl)
        saveToVersionControl.finalizeValues()
    }

    private fun properties() = listOf(
        instrumentationTaskName,
        applicationModuleName,
        applicationVariantName,
        macrobenchmarksOutputDirectory,
    )

    internal fun validateValues() {
        properties()
            .forEach {
                it.finalizeValueOnRead()
                require(it.isPresent) { "Property must be set - $it" }
            }
    }
}
