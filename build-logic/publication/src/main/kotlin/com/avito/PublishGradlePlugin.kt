package com.avito

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class PublishGradlePlugin : Plugin<Project> {

    private fun Project.gradlePlugin(configure: Action<GradlePluginDevelopmentExtension>) =
        extensions.configure("gradlePlugin", configure)

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishKotlinBase::class.java)
            plugins.apply(PublishReleasePlugin::class.java)
            plugins.apply("java-gradle-plugin")

            gradlePlugin {
                // we publish plugins as simple libraries for now,
                // someone should dive into plugin publication specifics
                // todo MBS-10660
                it.isAutomatedPublishing = false
            }

            val pluginPrefix = group.toString()

            publishing.apply {
                publications { pubs ->
                    afterEvaluate {
                        val gradlePlugins = extensions.getByType(GradlePluginDevelopmentExtension::class.java)
                        gradlePlugins.plugins.all { pluginDeclaration ->
                            pubs.register("${pluginDeclaration.name}PluginMaven", MavenPublication::class.java) {
                                it.from(components.getByName("java"))
                                require(pluginDeclaration.id.startsWith(pluginPrefix)) {
                                    "All avito plugins should be prefixed with $pluginPrefix"
                                }
                                val pluginName = pluginDeclaration.id.substringAfter("$pluginPrefix.")
                                if (pluginName != project.name) {
                                    logger.warn("For project `${project.name}` artifact id changed to `$pluginName`")
                                }
                                it.artifactId = pluginName
                            }
                        }
                    }
                }
            }
        }
    }
}
