package com.avito

import com.android.build.gradle.LibraryExtension
import com.avito.android.publish.AndroidLibraryPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/**
 * https://developer.android.com/build/publish-library/configure-pub-variants
 */
class PublishAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishReleasePlugin::class.java)

            val publishExtension = extensions.create("publish", AndroidLibraryPublishExtension::class.java)

            configure<LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }
            configure<PublishingExtension> {
                publications { publications ->
                    publications.register<MavenPublication>("releaseAndroidLibrary") {
                        afterEvaluate {
                            artifactId = publishExtension.artifactId.getOrElse(project.name)
                            from(components.getByName("release"))
                        }
                    }
                }
            }
        }
    }
}
