package com.avito.android.baseline_profile

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
