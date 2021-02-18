plugins {
    id("convention.publish-kotlin-base")
    id("convention.publish-release")
    id("convention.publish-artifactory")
    id("java-gradle-plugin")
}

gradlePlugin {
    // we publish plugins as simple libraries for now,
    // someone should dive into plugin publication specifics
    // todo MBS-10660
    isAutomatedPublishing = false
}

val pluginPrefix = group.toString()

publishing {
    publications {
        afterEvaluate {
            extensions.getByType<GradlePluginDevelopmentExtension>().plugins.all {

                val pluginDeclaration = this

                register<MavenPublication>("${pluginDeclaration.name}PluginMaven") {
                    from(components["java"])

                    require(pluginDeclaration.id.startsWith(pluginPrefix)) {
                        "All avito plugins should be prefixed with $pluginPrefix"
                    }

                    val pluginName = pluginDeclaration.id.substringAfter("$pluginPrefix.")

                    require(project.name == pluginName) {
                        // see isAutomatedPublishing note
                        "Plugin name should be equal to gradle module name to simplify resolving"
                    }

                    artifactId = pluginName
                }
            }
        }
    }
}
