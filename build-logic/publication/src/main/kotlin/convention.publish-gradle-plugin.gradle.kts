plugins {
    id("convention.publish-kotlin-base")
    id("convention.publish-release")
    id("java-gradle-plugin")
}

gradlePlugin {
    // we publish plugins as simple libraries for now,
    // someone should dive into plugin publication specifics
    // todo MBS-10660
    isAutomatedPublishing = false
}

val pluginPrefix = group.toString()

val moduleNamingExceptions = setOf(
    "legacy-signer", // will be removed in favor of new signer plugin
)

fun checkModuleToPluginNameConsistency(pluginName: String, pluginDeclaration: PluginDeclaration) {
    if (project.name !in moduleNamingExceptions) {
        require(project.name == pluginName) {
            // See isAutomatedPublishing comments above
            "Gradle plugin '${pluginDeclaration.id}' (${pluginDeclaration.name}) " +
                "must have id '$pluginPrefix.${project.name}'. " +
                "This is due to a publication issue: MBS-10660"
        }
    }
}

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

                    checkModuleToPluginNameConsistency(pluginName, pluginDeclaration)

                    artifactId = pluginName
                }
            }
        }
    }
}
