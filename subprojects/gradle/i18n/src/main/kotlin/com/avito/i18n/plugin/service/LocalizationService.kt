package com.avito.i18n.plugin.service

import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.tls.TlsCredentialsService
import com.avito.i18n.plugin.TranslationExtension
import com.avito.i18n.plugin.dto.OldTranslationRequest
import com.avito.i18n.plugin.dto.OldTranslationResponse
import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.dto.TranslationResponse
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class LocalizationService : BuildService<LocalizationService.Params> {

    internal interface Params : BuildServiceParameters {
        val serviceUrl: Property<String>
        val translateUrlPath: Property<String>
        val tlsCredentialsService: Property<TlsCredentialsService>
        val useTls: Property<Boolean>
    }

    private val serviceApi: LocalizationApi by lazy {
        LocalizationApiImpl(
            parameters.serviceUrl.get(),
            parameters.tlsCredentialsService.get(),
            parameters.translateUrlPath.get(),
            parameters.useTls.get()
        )
    }

    internal fun translateOld(
        file: OldTranslationRequest,
    ): OldTranslationResponse {
        return serviceApi.translateWithDeprecatedApi(file)
    }

    internal fun translate(
        request: TranslationRequest
    ): TranslationResponse {
        return serviceApi.translateWithNewApi(request)
    }

    companion object {
        fun provideService(project: Project): Provider<LocalizationService> {
            val extension = project.extensions.getByType(TranslationExtension::class.java)
            return project.gradle.sharedServices.registerIfAbsent(
                LocalizationService::class.java.name,
                LocalizationService::class.java
            ) {
                it.parameters.serviceUrl.set(extension.serviceUrl)
                it.parameters.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
                it.parameters.translateUrlPath.set(extension.translateUrlPath)
                it.parameters.useTls.set(extension.useTls)
            }
        }
    }
}
