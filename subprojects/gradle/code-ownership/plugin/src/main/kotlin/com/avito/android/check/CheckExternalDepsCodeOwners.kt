package com.avito.android.check

import com.avito.android.OwnerSerializer
import com.avito.android.diff.provider.OwnersProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

public abstract class CheckExternalDepsCodeOwners : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val libsOwnersFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val libsVersionsFile: RegularFileProperty

    @get:Internal
    public abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Internal
    public abstract val expectedOwnersProvider: Property<OwnersProvider>

    @TaskAction
    public fun checkDependencies() {
        val versionsFile = libsVersionsFile.asFile.orNull
        val ownersFile = libsOwnersFile.asFile.orNull

        require(versionsFile != null && versionsFile.exists()) {
            propertyRequiredMessage("ownership.externalDependencies.libsVersionsFile") +
                "It must contain a valid toml file with external dependencies config.\n" +
                "Format is described here: $TOML_DEPENDENCIES_LINK"
        }

        require(ownersFile != null && ownersFile.exists()) {
            propertyRequiredMessage("ownership.externalDependencies.libsOwnersFile") +
                """
                It must contain a valid toml file with external dependencies config.
                Format is similar to format of libs.versions.toml, but instead of dependency you must declare an owner:
                 
                [libraries]
                android-constraintLayout = "OwnerName"
                """.trimIndent()
        }
        val ownerSerializer = ownerSerializer.orNull
            ?: throwRequiredPropertyError("ownership.ownersSerializer")
        val validOwnersProvider = expectedOwnersProvider.orNull
            ?: throwRequiredPropertyError("codeOwnershipDiffReport.expectedOwnersProvider")
        val checker = ExternalDepsCodeOwnersChecker(ownerSerializer, validOwnersProvider)
        checker.check(versionsFile, ownersFile)
    }

    private fun throwRequiredPropertyError(propertyName: String): Nothing {
        error(propertyRequiredMessage(propertyName))
    }

    private fun propertyRequiredMessage(propertyName: String): String =
        "You must specify $propertyName property to run CheckExternalDepsCodeOwners task. "

    public companion object {
        public const val NAME: String = "checkExternalDepsCodeOwners"
        private const val TOML_DEPENDENCIES_LINK =
            "https://docs.gradle.org/current/userguide/platforms.html#sub:conventional-dependencies-toml"
    }
}
