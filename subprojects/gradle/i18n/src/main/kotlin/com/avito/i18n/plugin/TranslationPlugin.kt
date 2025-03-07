package com.avito.i18n.plugin

import com.avito.android.isAndroid
import com.avito.i18n.plugin.service.LocalizationService
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

public class TranslationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        check(!target.isRoot()) {
            "TranslationPlugin should not be applied to a root project"
        }
        check(target.isAndroid()) {
            "TranslationPlugin must be applied to Android modules"
        }

        val translationExtension = target.extensions.create<TranslationExtension>("translation")

        target.extensions.configure(TranslationExtension::class.java) {
            it.componentName.set(target.getComponentName())
        }
        val service = LocalizationService.provideService(target)

        configureTranslationTask(target, translationExtension, service)
    }

    private fun configureTranslationTask(
        target: Project,
        translationExtension: TranslationExtension,
        localizationService: Provider<LocalizationService>
    ) {
        target.tasks.register<TranslationFileTask>(TRANSLATION_TASK_NAME) {
            val resDir = checkNotNull(target.mainResDir) {
                "'resDir' not found!"
            }
            val file = File(resDir, DEFAULT_STRING_FILE)
            check(file.exists()) {
                "File '$DEFAULT_STRING_FILE' not found!"
            }
            defaultStringsFile.set(file)
            service.set(localizationService)
            locales.set(translationExtension.locales)
            namespace.set(translationExtension.namespace)
            sourceLocale.set(translationExtension.sourceLocale)
            componentName.set(translationExtension.componentName)
            usesService(localizationService)
        }
    }

    private fun Project.getComponentName(): String {
        return path
    }

    internal companion object {
        const val TRANSLATION_TASK_NAME = "updateTranslations"
        const val DEFAULT_STRING_FILE = "values/strings.xml"
    }
}
