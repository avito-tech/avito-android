package com.avito.i18n.plugin

import com.avito.i18n.plugin.internal.OldStringsFileTranslator
import com.avito.i18n.plugin.internal.StringsFileTranslator
import com.avito.i18n.plugin.internal.TranslationApiInteractor
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
internal abstract class TranslationFileTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val defaultStringsFile: RegularFileProperty

    @get:Input
    abstract val locales: SetProperty<String>

    @get:Input
    abstract val namespace: Property<String>

    @get:Input
    abstract val sourceLocale: Property<String>

    @get:Input
    abstract val componentName: Property<String>

    @get:Input
    abstract val useNewApi: Property<Boolean>

    @get:Internal
    abstract val service: Property<LocalizationService>

    @TaskAction
    fun doTranslateFile() {
        if (useNewApi.get()) {
            StringsFileTranslator(
                defaultStringsFile = defaultStringsFile.get().asFile,
                locales = locales.get(),
                apiInteractor = TranslationApiInteractor(
                    namespace = namespace.get(),
                    sourceLocale = sourceLocale.get(),
                    componentName = componentName.get(),
                    service = service.get()
                )
            ).translate()
        } else {
            OldStringsFileTranslator(
                defaultStringsFile = defaultStringsFile.get().asFile,
                locales = locales.get(),
                namespace = namespace.get(),
                sourceLocale = sourceLocale.get(),
                componentName = componentName.get(),
                service = service.get()
            ).translate()
        }
    }
}
