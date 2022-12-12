package com.avito.android.check.ownersip

import com.avito.android.model.Owner
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class CheckOwnersPresentTask : DefaultTask() {

    @get:Input
    public abstract val owners: SetProperty<Owner>

    @get:Input
    public abstract val emptyOwnersErrorMessage: Property<String>

    @get:Input
    public abstract val projectPath: Property<String>

    @get:OutputFile
    public val outputFile: RegularFileProperty =
        project.objects.fileProperty().apply { set(project.layout.buildDirectory.file("owners-present.output")) }

    @TaskAction
    public fun check() {
        val codeOwners = owners.get()
        if (codeOwners.isEmpty()) {
            val emptyOwnersMessage = emptyOwnersErrorMessage.get()
            throw IllegalStateException(emptyOwnersMessage.format(projectPath.get()))
        }
        outputFile.get().asFile.writeText("Owners: " + codeOwners.joinToString())
    }
}
