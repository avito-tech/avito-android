package com.avito.android.check.deps

import com.avito.android.OwnerSerializer
import com.avito.android.diff.provider.OwnersProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
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

    @get:OutputFile
    @get:Optional
    public abstract val reportFile: RegularFileProperty

    @TaskAction
    public fun checkDependencies() {
        val versionsFile = libsVersionsFile.asFile.get()
        val ownersFile = libsOwnersFile.asFile.get()

        val ownerSerializer = ownerSerializer.orNull
            ?: throwRequiredPropertyError("ownership.ownersSerializer")
        val validOwnersProvider = expectedOwnersProvider.orNull
            ?: throwRequiredPropertyError("codeOwnershipDiffReport.expectedOwnersProvider")
        val checker = ExternalDepsCodeOwnersChecker(ownerSerializer, validOwnersProvider)
        checker.check(versionsFile, ownersFile)
        reportToFile("External owners check successful. No errors found.")
    }

    private fun reportToFile(message: String) {
        val reportFile = reportFile.get().asFile
        reportFile.createNewFile()
        reportFile.writeText(message)
    }

    private fun throwRequiredPropertyError(propertyName: String): Nothing {
        val message = propertyRequiredMessage(propertyName)
        reportToFile(message)
        error(propertyRequiredMessage(propertyName))
    }

    private fun propertyRequiredMessage(propertyName: String): String =
        "You must specify $propertyName property to run CheckExternalDepsCodeOwners task. "

    public companion object {
        public const val NAME: String = "checkExternalDepsCodeOwners"
    }
}
