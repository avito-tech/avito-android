package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.TranslationExtension
import com.avito.i18n.plugin.xml.StringsFile
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class LocalizationService : BuildService<LocalizationService.Params> {

    internal interface Params : BuildServiceParameters {
        val serviceUrl: Property<String>
    }

    private val serviceApi: LocalizationApi by lazy { FakeLocalizationApi() }

    internal fun translate(
        file: StringsFile,
        locales: Set<String>
    ): List<StringsFile> {
        return serviceApi.translate(file, locales)
    }

    internal fun createComponent(
        name: String,
        file: String,
        locales: Set<String>
    ) {
        serviceApi.createComponent(name, file, locales)
    }

    companion object {
        fun provideService(project: Project): Provider<LocalizationService> {
            val extension = project.extensions.getByType(TranslationExtension::class.java)
            return project.gradle.sharedServices.registerIfAbsent(
                LocalizationService::class.java.name,
                LocalizationService::class.java
            ) {
                it.parameters.serviceUrl.set(extension.serviceUrl)
            }
        }
    }
}
