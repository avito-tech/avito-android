package com.avito.android

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.LibraryVariantBuilder
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.ProcessJavaResTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.WriteProperties
import java.io.File
import java.util.jar.Attributes

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("com.android.library")
            plugins.apply(AndroidBasePlugin::class.java)
            plugins.apply(KotlinAndroidBasePlugin::class.java)
            plugins.apply("convention.dependency-locking-android-lib")

            val generatedJavaResDir: Provider<RegularFile> =
                project.layout.buildDirectory.file("generated/avito/java_res")

            extensions.configure(AndroidComponentsExtension::class.java) {
                it.beforeVariants { variant ->
                    variant as LibraryVariantBuilder
                    /**
                     * Ignore all buildTypes instead of release for com.android.library modules
                     * Also configure fallbacks for dependent modules
                     */
                    if (variant.name != "release") {
                        variant.enable = false
                    } else {
                        variant.enableAndroidTest = false
                    }
                }
            }
            extensions.configure(LibraryExtension::class.java) { android ->
                with(android) {

                    @Suppress("UnstableApiUsage")
                    sourceSets {
                        getByName("main").resources.srcDir(generatedJavaResDir.get().asFile)
                    }
                }
            }

            val generateLibraryJavaResProvider: TaskProvider<WriteProperties> =
                project.tasks.register("generateLibraryJavaRes", WriteProperties::class.java) {
                    // Don't use MANIFEST.MF to avoid clashing and rewriting in packaging
                    val projectUniqueProperties = "META-INF/com.avito.android.${project.name}.properties"
                    it.outputFile = File(generatedJavaResDir.get().asFile, projectUniqueProperties)
                    it.property(Attributes.Name.IMPLEMENTATION_VERSION.toString(), project.version.toString())
                }

            project.tasks.withType(ProcessJavaResTask::class.java).configureEach {
                it.dependsOn(generateLibraryJavaResProvider)
            }
        }
    }
}
