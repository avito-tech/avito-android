package com.avito.i18n.plugin

import com.avito.android.Problem
import com.avito.android.asPlainText
import com.avito.i18n.plugin.internal.StringsFileTranslator
import com.avito.i18n.plugin.internal.TranslationApiInteractor
import com.avito.i18n.plugin.service.LocalizationService
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

// The task is not cached. The output depends on the response from the backend.
internal abstract class TranslationFileTask : DefaultTask() {

    @get:InputFiles
    abstract val defaultStringsFiles: ConfigurableFileCollection

    @get:Input
    abstract val locales: SetProperty<String>

    @get:Input
    abstract val namespace: Property<String>

    @get:Input
    abstract val sourceLocale: Property<String>

    @get:Input
    abstract val componentName: Property<String>

    @get:Internal
    abstract val service: Property<LocalizationService>

    @TaskAction
    fun doTranslateFile() {
        defaultStringsFiles.forEach { stringsFile ->
            StringsFileTranslator(
                defaultStringsFile = stringsFile,
                locales = locales.get(),
                apiInteractor = TranslationApiInteractor(
                    namespace = namespace.get(),
                    sourceLocale = sourceLocale.get(),
                    componentName = componentName.get(),
                    service = service.get()
                )
            ).translate()
                .getOrElse { e ->
                    throw GradleException(
                        Problem.Builder(
                            shortDescription = "Translation failed for '${stringsFile.name}'",
                            context = "Task '$name' translating ${stringsFile.path}"
                        )
                            .throwable(e)
                            .build()
                            .asPlainText(),
                        e
                    )
                }
        }
    }
}
