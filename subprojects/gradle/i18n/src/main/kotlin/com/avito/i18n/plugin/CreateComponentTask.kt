package com.avito.i18n.plugin

import com.avito.i18n.plugin.service.LocalizationService
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// The task is not cached. The output depends on the response from the backend.
internal abstract class CreateComponentTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stringsFilePath: RegularFileProperty

    @get:Input
    abstract val locales: SetProperty<String>

    @get:Input
    abstract val componentName: Property<String>

    @get:Internal
    abstract val service: Property<LocalizationService>

    @TaskAction
    fun doCreateComponent() {
        val service = service.get()
        service.createComponent(
            name = componentName.get(),
            file = stringsFilePath.asFile.get().path,
            locales = locales.get(),
        )
    }
}
