package com.avito.android.network_contracts

import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.extension.urls.ArtifactoryUrlConfiguration
import com.avito.android.network_contracts.extension.urls.ServiceUrlConfiguration
import com.avito.android.network_contracts.internal.http.HttpClientBuilder
import com.avito.android.network_contracts.internal.http.provideHttpClientBuilder
import com.avito.android.network_contracts.internal.http.provideTlsManager
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.create

public class NetworkContractsModulePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val networkContractsExtension = target.extensions
            .create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME)

        networkContractsExtension.registerUrlConfigurations(target.objects)
    }
}

private fun NetworkContractsModuleExtension.registerUrlConfigurations(objects: ObjectFactory) {
    urls.registerFactory(ArtifactoryUrlConfiguration::class.java) {
        objects.newInstance(ArtifactoryUrlConfiguration::class.java, it)
    }
    urls.registerFactory(ServiceUrlConfiguration::class.java) {
        objects.newInstance(ServiceUrlConfiguration::class.java, it)
    }
}

private fun Project.provideHttpClientBuilder(extension: NetworkContractsModuleExtension): HttpClientBuilder {
    return provideHttpClientBuilder {
        val serviceUrl = extension.urls.named("serviceUrl", ServiceUrlConfiguration::class.java)
        require(serviceUrl.isPresent) {
            """
                Unable to find serviceUrl to configure http client. 
                Please, register `serviceUrl` provider to `${NetworkContractsModuleExtension.NAME}.urls`.
            """.trimIndent()
        }

        this.serviceUrl.set(serviceUrl.flatMap { it.serviceUrl })
        loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(project))
        tlsManager.set(provideTlsManager(extension.useTls))
    }
}
