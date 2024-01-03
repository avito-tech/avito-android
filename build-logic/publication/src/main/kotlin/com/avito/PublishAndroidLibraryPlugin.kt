package com.avito

import com.android.build.gradle.LibraryExtension
import com.avito.android.publish.AndroidLibraryPublishExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class PublishAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(PublishReleasePlugin::class.java)

            val publishExtension = extensions.create("publish", AndroidLibraryPublishExtension::class.java)

            extensions.configure(LibraryExtension::class.java) { libraryExt ->
                with(libraryExt) {
                    val sourcesTask = tasks.register("sourcesJar", Jar::class.java) {
                        it.archiveClassifier.set("sources")
                        it.from(libraryExt.sourceSets.getByName("main").java.srcDirs)
                    }

                    val allVariantNames = mutableListOf<String>()
                    var registeredVariants = 0

                    // todo use new publishing: https://developer.android.com/studio/releases/gradle-plugin#build-variant-publishing
                    libraryVariants
                        .matching {
                            allVariantNames += it.name
                            it.name == publishExtension.variant.get()
                        }.whenObjectAdded {
                            extensions.configure(PublishingExtension::class.java) { publishing ->
                                publishing.publications { pubs ->
                                    pubs.register(
                                        "${publishExtension.variant.get()}AndroidLibrary",
                                        MavenPublication::class.java
                                    ) { maven ->
                                        maven.from(components.getAt(publishExtension.variant.get()))
                                        maven.artifact(sourcesTask.get())

                                        registeredVariants++

                                        afterEvaluate {
                                            maven.artifactId = publishExtension.artifactId.getOrElse(project.name)
                                        }
                                    }
                                }
                            }
                        }

                    afterEvaluate {
                        require(registeredVariants > 0) {
                            val path = project.path
                            val variant = publishExtension.variant.get()
                            """
                             No created publications for $path, with plugin "convention.publish-android-library". 
                             Options:
                                   - Remove plugin if library was not supposed to be published
                                   - Check configuration: published variant:$variant;available variants=$allVariantNames
                            """.trimIndent()
                        }
                    }
                }
            }
        }
    }
}
