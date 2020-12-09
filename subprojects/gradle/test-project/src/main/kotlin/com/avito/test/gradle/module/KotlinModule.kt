package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dir
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import java.io.File

class KotlinModule(
    override val name: String,
    val packageName: String = "com.$name",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val dependencies: Set<GradleDependency> = emptySet(),
    private val mutator: File.() -> Unit = {}
) : Module {

    override fun generateIn(file: File) {
        file.module(name) {

            build_gradle {
                writeText(
                    """
plugins {
    id 'kotlin'
    ${plugins.joinToString(separator = "\n") { "id '$it'" }}
}

$buildGradleExtra

dependencies {
    ${dependencies.joinToString(separator = "\n\t", transform = { it.getScriptRepresentation() })}
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
""".trimIndent()
                )
            }
            dir("src/main") {
                dir("kotlin") {
                    kotlinClass("SomeClass", packageName)
                }
            }
            this.mutator()
        }
    }
}
