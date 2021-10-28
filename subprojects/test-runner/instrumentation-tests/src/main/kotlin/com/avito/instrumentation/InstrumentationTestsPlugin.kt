package com.avito.instrumentation

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BasePlugin
import com.avito.android.stats.statsdConfig
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation.internal.ConfiguratorsFactory
import com.avito.instrumentation.internal.InstrumentationTaskConfigurator
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.buildFailer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

public class InstrumentationTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.createInstrumentationPluginExtension()

        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)

        val factory = ConfiguratorsFactory(project, extension, loggerFactory)

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

                // todo how to write "only testBuildType" selector?
                // todo support flavors
                androidComponents.onVariants { variant ->

                    val configurators = factory.createTaskConfigurators(configuration, variant)

                    if (configurators != null) {
                        project.tasks.register(
                            instrumentationTaskName(configuration.name),
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
            loggerFactory.set(
                GradleLoggerFactory.fromTask(
                    project = project,
                    taskName = this.name,
                )
            )
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
        return extension
    }
}
