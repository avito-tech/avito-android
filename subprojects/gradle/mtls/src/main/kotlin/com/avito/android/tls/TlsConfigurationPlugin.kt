package com.avito.android.tls

import com.avito.android.tls.extensions.TlsConfigurationExtension
import com.avito.android.tls.extensions.TlsCredentialsRegister
import com.avito.android.tls.extensions.configuration.DirectoryTlsCredentialsConfiguration
import com.avito.android.tls.extensions.configuration.FilesTlsCredentialsConfiguration
import com.avito.android.tls.extensions.configuration.RawContentTlsCredentialsConfiguration
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

public class TlsConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) {
            "Plugin must be applied to the root project, but was applied to ${target.path}"
        }

        val extension = target.extensions.create("tls", TlsConfigurationExtension::class.java)
        extension.credentials.registerTlsCredentialsConfigurationsFactory(target.objects)
    }

    public companion object {

        public fun provideCredentialsService(project: Project): Provider<TlsCredentialsService> {
            if (!project.rootProject.plugins.hasPlugin(TlsConfigurationPlugin::class.java)) {
                throw IllegalStateException(
                    "Failed to provideCredentialsService for project ${project.name}." +
                        "Apply com.avito.android.tls-configuration plugin to the root project"
                )
            }

            val tlsExtension = project.rootProject.extensions.getByType<TlsConfigurationExtension>()
            val service = project.gradle.sharedServices.registerIfAbsent(
                TlsCredentialsService::class.java.name,
                TlsCredentialsService::class.java,
            ) {
                it.parameters { params ->
                    params.configurations.set(tlsExtension.credentials.tlsCredentialsProviders)
                    params.docsUrl.set(tlsExtension.docsUrl)
                    params.verbose.set(tlsExtension.verbose)
                }
            }

            return service
        }
    }
}

private fun TlsCredentialsRegister.registerTlsCredentialsConfigurationsFactory(objects: ObjectFactory) {
    tlsCredentialsProviders.registerFactory(RawContentTlsCredentialsConfiguration::class.java) {
        objects.newInstance(RawContentTlsCredentialsConfiguration::class.java, it)
    }
    tlsCredentialsProviders.registerFactory(FilesTlsCredentialsConfiguration::class.java) {
        objects.newInstance(FilesTlsCredentialsConfiguration::class.java, it)
    }
    tlsCredentialsProviders.registerFactory(DirectoryTlsCredentialsConfiguration::class.java) {
        objects.newInstance(DirectoryTlsCredentialsConfiguration::class.java, it)
    }
}
