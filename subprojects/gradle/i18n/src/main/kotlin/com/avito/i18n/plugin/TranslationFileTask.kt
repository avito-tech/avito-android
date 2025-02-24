package com.avito.i18n.plugin

import com.avito.i18n.plugin.service.LocalizationService
import com.avito.i18n.plugin.xml.StringsFile
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
internal abstract class TranslationFileTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val defaultStringsFile: RegularFileProperty

    @get:Input
    abstract val locales: SetProperty<String>

    @get:Internal
    abstract val service: Property<LocalizationService>

    @TaskAction
    fun doTranslateFile() {
        val original = StringsFile(defaultStringsFile.asFile.get())
        service.get()
            .translate(original, locales.get())
            .forEach { f -> f.writeFile() }
    }
}
