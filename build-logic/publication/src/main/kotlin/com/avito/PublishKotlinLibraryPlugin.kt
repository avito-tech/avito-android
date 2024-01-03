package com.avito

import com.avito.android.publish.KotlinLibraryPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PublishKotlinLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishKotlinBase::class.java)
            plugins.apply(PublishReleasePlugin::class.java)

            val publishExtension = extensions.create("publish", KotlinLibraryPublishExtension::class.java)

            publishing.apply {
                publications { publications ->
                    publications.register("kotlinLibraryMaven", MavenPublication::class.java) { maven ->
                        maven.from(components.getByName("java"))

                        afterEvaluate {
                            maven.artifactId = publishExtension.artifactId.getOrElse(project.name)
                        }
                    }
                }
            }
        }
    }
}
