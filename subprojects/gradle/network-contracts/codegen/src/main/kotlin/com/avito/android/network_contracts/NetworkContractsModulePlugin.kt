package com.avito.android.network_contracts

import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.extension.urls.ArtifactoryUrlConfiguration
import com.avito.android.network_contracts.extension.urls.ServiceUrlConfiguration
import com.avito.android.network_contracts.internal.http.HttpClientBuilder
import com.avito.android.network_contracts.internal.http.provideHttpClientBuilder
import com.avito.android.network_contracts.internal.http.provideTlsManager
import com.avito.android.network_contracts.scheme.imports.ApiSchemesImportTask
import com.avito.android.network_contracts.utils.findPackageDirectory
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class NetworkContractsModulePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val networkContractsExtension = target.extensions
            .create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME)

        networkContractsExtension.registerUrlConfigurations(target.objects)
        val httpClientBuilder = target.configureHttpClientBuilder(networkContractsExtension)

        target.registerAddEndpointTask(networkContractsExtension, httpClientBuilder)
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

private fun Project.configureHttpClientBuilder(
    extension: NetworkContractsModuleExtension
): Provider<HttpClientBuilder> {
    return provideHttpClientBuilder {
        val serviceUrl = extension.urls.named(
            NetworkContractsModuleExtension.SERVICE_URL_NAME,
            ServiceUrlConfiguration::class.java
        )
        this.serviceUrl.set(serviceUrl.flatMap { it.serviceUrl })
        loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(project))
        tlsManager.set(provideTlsManager(extension.useTls))
    }
}

private fun Project.registerAddEndpointTask(
    extension: NetworkContractsModuleExtension,
    httpClientBuilder: Provider<HttpClientBuilder>,
) {
    tasks.register<ApiSchemesImportTask>("addEndpoint") {
        if (!project.hasProperty("apiSchemesUrl")) {
            error(
                "Parameter `apiSchemesUrl` is not specified. " +
                    "Run task with parameter `-PapiSchemesUrl` with desired path."
            )
        }
        apiPath.set(project.getMandatoryStringProperty("apiSchemesUrl"))
        outputDirectory.set(project.findPackageDirectory(extension.packageName.get()))
        this.httpClientBuilder.set(httpClientBuilder)
        loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(this))
    }
}
