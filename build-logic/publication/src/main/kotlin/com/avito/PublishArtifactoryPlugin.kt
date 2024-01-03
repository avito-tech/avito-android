package com.avito

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

class PublishArtifactoryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishBasePlugin::class.java)
            val artifactoryUrlProperty: Provider<String> = providers.gradleProperty("artifactoryUrl")

            val artifactoryUser: Provider<String> = providers.gradleProperty("avito.artifactory.user")

            val artifactoryPassword: Provider<String> = providers.gradleProperty("avito.artifactory.password")

            val repoName = "Artifactory"

            tasks.register("publishToArtifactory") {
                it.group = "publication"
                it.dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
            }

            publishing.apply {
                repositories { repos ->
                    with(repos) {
                        maven {
                            with(it) {
                                name = repoName
                                val artifactoryUrl = artifactoryUrlProperty.orNull
                                if (!artifactoryUrl.isNullOrBlank()) {
                                    setUrl("${artifactoryUrl.removeSuffix("/")}/libs-release-local")
                                }
                                credentials {
                                    it.username = artifactoryUser.orNull
                                    it.password = artifactoryPassword.orNull
                                }

                                isAllowInsecureProtocol = true
                            }
                        }
                    }
                }
            }
        }
    }
}
