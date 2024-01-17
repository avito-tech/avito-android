package com.avito.android.baseline_profile.configuration

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class SaveProfileToVersionControlExtension(
    objects: ObjectFactory,
) {
    public val enable: Property<Boolean> = objects.property<Boolean>().convention(false)

    public val enableRemoteOperations: Property<Boolean> = objects.property<Boolean>().convention(true)

    public abstract val commitMessage: Property<String>

    public val includeDetailsInCommitMessage: Property<Boolean> = objects.property<Boolean>().convention(true)

    internal fun finalizeValues() {
        listOf(
            enable,
            enableRemoteOperations,
            commitMessage,
            includeDetailsInCommitMessage
        ).forEach { property ->
            require(property.isPresent) { "Property must be set - $property" }
            property.finalizeValueOnRead()
        }
    }
}

public interface SaveProfileToVersionControlSettings {
    public val enable: Boolean
    public val enableRemoteOperations: Boolean
    public val commitMessage: String
    public val includeDetailsInCommitMessage: Boolean
}

internal class SaveProfileToVcsSettingsFromExtension(
    private val extension: SaveProfileToVersionControlExtension,
) : SaveProfileToVersionControlSettings {
    override val enable: Boolean
        get() = extension.enable.get()
    override val enableRemoteOperations: Boolean
        get() = extension.enableRemoteOperations.get()
    override val commitMessage: String
        get() = extension.commitMessage.get()
    override val includeDetailsInCommitMessage: Boolean
        get() = extension.includeDetailsInCommitMessage.get()
}
