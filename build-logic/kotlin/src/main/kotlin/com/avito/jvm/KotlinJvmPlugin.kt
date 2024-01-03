package com.avito.jvm

import com.avito.android.withVersionCatalog
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinJvmPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.apply(KotlinJvmBasePlugin::class.java)
            withVersionCatalog { libs ->
                val javaTarget = JavaLanguageVersion.of(libs.versions.java.get()).toString()

                tasks.withType(JavaCompile::class.java) {
                    it.sourceCompatibility = javaTarget
                    it.targetCompatibility = javaTarget
                }

                tasks.withType(KotlinCompile::class.java).configureEach {
                    it.kotlinOptions {
                        jvmTarget = javaTarget
                    }
                }
            }
        }
    }
}
