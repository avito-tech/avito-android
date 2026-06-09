package com.avito.i18n.plugin

import com.android.build.api.variant.AndroidComponentsExtension
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

        verifyStringsFilesExist(target, translationExtension)
        configureTranslationTask(target, translationExtension, service)
    }

    private fun verifyStringsFilesExist(target: Project, translationExtension: TranslationExtension) {
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.finalizeDsl {
            target.resolveStringsFiles(translationExtension).forEach { file ->
                check(file.exists()) {
                    missingStringsFileMessage(target.path, file)
                }
            }
        }
    }

    private fun configureTranslationTask(
        target: Project,
        translationExtension: TranslationExtension,
        localizationService: Provider<LocalizationService>
    ) {
        target.tasks.register<TranslationFileTask>(TRANSLATION_TASK_NAME) {
            defaultStringsFiles.setFrom(target.resolveStringsFiles(translationExtension))
            service.set(localizationService)
            locales.set(translationExtension.locales)
            namespace.set(translationExtension.namespace)
            sourceLocale.set(translationExtension.sourceLocale)
            componentName.set(translationExtension.componentName)
            usesService(localizationService)
        }
    }

    private fun Project.resolveStringsFiles(translationExtension: TranslationExtension): List<File> {
        val mainResDir = checkNotNull(mainResDir) {
            "'mainResDir' not found!"
        }
        val resDirsToTranslate = translationExtension.flavorNamesToTranslate.get()
            .map { flavorName -> getResDirByName(flavorName) }
            .takeUnless { it.isEmpty() }
            ?: listOf(mainResDir)

        return resDirsToTranslate.filterNotNull()
            .map { resDir -> File(resDir, DEFAULT_STRING_FILE) }
    }

    private fun missingStringsFileMessage(modulePath: String, file: File): String =
        "Module '$modulePath' applies the i18n plugin (com.avito.android.i18n) " +
            "but '$DEFAULT_STRING_FILE' is missing: '${file.path}'.\n" +
            "Fix it by either:\n" +
            "  - removing the i18n plugin from the module's build.gradle, or\n" +
            "  - restoring the '$DEFAULT_STRING_FILE' file."

    private fun Project.getComponentName(): String {
        return path
    }

    internal companion object {
        const val TRANSLATION_TASK_NAME = "updateTranslations"
        const val DEFAULT_STRING_FILE = "values/strings.xml"
    }
}
