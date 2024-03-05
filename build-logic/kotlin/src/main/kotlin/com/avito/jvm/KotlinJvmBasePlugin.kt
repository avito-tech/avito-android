package com.avito.jvm

import com.avito.KotlinBasePlugin
import com.avito.android.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.util.jar.Attributes

class KotlinJvmBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("kotlin")
            plugins.apply(KotlinBasePlugin::class.java)
            plugins.apply("convention.dependency-locking-kotlin")

            val javaTarget = JavaLanguageVersion.of(libs.versions.java.get())
            extensions.configure(KotlinJvmProjectExtension::class.java) { kotlin ->
                kotlin.explicitApi()
                kotlin.jvmToolchain {
                    it.languageVersion.set(javaTarget)
                }
            }

            tasks.withType(Jar::class.java).configureEach { task ->
                task.manifest {
                    it.attributes(
                        mapOf(
                            // To access a build version in runtime through class.java.`package`.implementationVersion
                            Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version
                        )
                    )
                }
            }
        }
    }
}
