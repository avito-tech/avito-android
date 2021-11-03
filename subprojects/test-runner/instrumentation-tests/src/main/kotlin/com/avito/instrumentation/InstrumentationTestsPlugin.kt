package com.avito.instrumentation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BasePlugin
import com.avito.android.stats.statsdConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.configuration.KubernetesViaContext
import com.avito.instrumentation.configuration.KubernetesViaCredentials
import com.avito.instrumentation.configuration.LocalAdb
import com.avito.instrumentation.internal.ConfiguratorsFactory
import com.avito.instrumentation.internal.InstrumentationTaskConfigurator
import com.avito.instrumentation.internal.TaskValidatorsFactory
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.utils.buildFailer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

public class InstrumentationTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.createInstrumentationPluginExtension()

        val factory = ConfiguratorsFactory(project, extension)

        val filtersFactory = TaskValidatorsFactory()

        project.plugins.withType<BasePlugin> {

            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.finalizeDsl { androidExtension ->

                val instrumentationArgs = factory.instrumentationArgsResolver.resolvePluginLevelArgs(
                    project = project,
                    androidExtension = androidExtension
                )

                factory.localRunInteractor.setupLocalRunInstrumentationArgs(
                    androidExtension = androidExtension,
                    args = instrumentationArgs
                )
            }

            extension.configurationsContainer.all { configuration ->

                registerDefaultEnvironment(
                    providers = project.providers,
                    extension = extension,
                    configuration = configuration
                )

                extension.environmentsContainer.all { environment ->

                    // todo how to write "only testBuildType" selector?

                    androidComponents.onVariants { variant ->

                        val configurators = factory.createTaskConfigurators(
                            configuration = configuration,
                            environment = environment,
                            variant = variant
                        )

                        val filters = filtersFactory.create()

                        if (configurators != null && filters.all { it.filter(configuration, environment) }) {

                            project.tasks.register(
                                instrumentationTaskName(
                                    configuration = configuration.name,
                                    environment = environment.name,
                                    flavor = variant.flavorName
                                ),
                                configureInstrumentationTask(
                                    configurators = configurators,
                                    configuration = configuration,
                                    extension = extension,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun configureInstrumentationTask(
        configurators: List<InstrumentationTaskConfigurator>,
        configuration: InstrumentationConfiguration,
        extension: InstrumentationTestsPluginExtension,
    ): InstrumentationTestsTask.() -> Unit {
        return {
            timeout.set(configuration.instrumentationTaskTimeout)
            group = CI_TASK_GROUP

            projectName.set(project.name)
            statsDConfig.set(project.statsdConfig)
            buildFailer.set(project.buildFailer)
            gradleTestKitRun.set(project.getBooleanProperty("isGradleTestKitRun"))
            logcatTags.set(extension.logcatTags)
            enableDeviceDebug.set(configuration.enableDeviceDebug)

            configurators.forEach {
                it.configure(this)
            }
        }
    }

    private fun Project.createInstrumentationPluginExtension(): InstrumentationTestsPluginExtension {
        val extension = extensions.create<InstrumentationTestsPluginExtension>("instrumentation")

        extension.filters.register("default") {
            it.fromRunHistory.excludePreviousStatuses(
                setOf(
                    InstrumentationFilter.FromRunHistory.RunStatus.Manual,
                    InstrumentationFilter.FromRunHistory.RunStatus.Success
                )
            )
        }

        extension.environmentsContainer.registerFactory(KubernetesViaCredentials::class.java) {
            project.objects.newInstance(KubernetesViaCredentials::class.java, it)
        }
        extension.environmentsContainer.registerFactory(KubernetesViaContext::class.java) {
            project.objects.newInstance(KubernetesViaContext::class.java, it)
        }
        extension.environmentsContainer.registerFactory(LocalAdb::class.java) {
            project.objects.newInstance(LocalAdb::class.java, it)
        }

        extension.environmentsContainer.register<LocalAdb>("local")

        return extension
    }

    /**
     * todo remove (registered for backward compatibility)
     */
    private fun registerDefaultEnvironment(
        providers: ProviderFactory,
        extension: InstrumentationTestsPluginExtension,
        configuration: InstrumentationConfiguration
    ) {
        if (extension.environmentsContainer.findByName(ENVIRONMENT_DEFAULT) == null) {
            extension.environmentsContainer.register<KubernetesViaCredentials>(ENVIRONMENT_DEFAULT) {
                url.set(providers.gradleProperty("kubernetesUrl").forUseAtConfigurationTime())
                token.set(providers.gradleProperty("kubernetesToken").forUseAtConfigurationTime())
                caCertData.set(providers.gradleProperty("kubernetesCaCertData").forUseAtConfigurationTime())
                @Suppress("DEPRECATION")
                namespace.set(configuration.kubernetesNamespace)
            }
        }
    }
}
