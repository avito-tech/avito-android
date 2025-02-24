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
        configureCreateComponentTask(target, translationExtension, service)
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
            val stringsFile = translationExtension.defaultLocale.get().getStringsFile()
            val file = File(resDir, stringsFile)
            check(file.exists()) {
                "File '$stringsFile' not found!"
            }
            defaultStringsFile.set(file)
            service.set(localizationService)
            locales.set(translationExtension.locales)
            usesService(localizationService)
        }
    }

    private fun configureCreateComponentTask(
        target: Project,
        translationExtension: TranslationExtension,
        localizationService: Provider<LocalizationService>
    ) {
        target.tasks.register<CreateComponentTask>(CREATE_COMPONENT_TASK_NAME) {
            val resDir = checkNotNull(target.mainResDir) {
                "'resDir' not found!"
            }
            val stringsFile = translationExtension.defaultLocale.get().getStringsFile()
            val file = File(resDir, stringsFile)
            check(file.exists()) {
                "File '$stringsFile' not found!"
            }
            stringsFilePath.set(file)
            locales.set(translationExtension.locales)
            service.set(localizationService)
            componentName.set(translationExtension.componentName)
            usesService(localizationService)
        }
    }

    private fun Project.getComponentName(): String {
        return path
    }

    internal companion object {
        const val TRANSLATION_TASK_NAME = "updateTranslations"
        const val CREATE_COMPONENT_TASK_NAME = "createTranslationComponent"
        const val RES_PATH = "src/main/res/"
    }
}
