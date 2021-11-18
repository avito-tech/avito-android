package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dir
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import java.io.File

public class KotlinModule(
    override val name: String,
    public val packageName: String = "com.$name",
    public override val imports: List<String> = emptyList(),
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val dependencies: Set<GradleDependency> = emptySet(),
    override val useKts: Boolean = false,
    private val mutator: File.() -> Unit = {},
) : Module {

    override fun generateIn(file: File) {
        file.module(name) {

            val buildGradleContent = """
                |${imports()}
                |${plugins()}
                |
                |$buildGradleExtra
                |
                |dependencies {
                |   ${dependencies.joinToString(separator = "\n\t", transform = { it.getScriptRepresentation() })}
                |   implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                |}
                """.trimMargin()

            if (useKts) {
                build_gradle_kts {
                    writeText(buildGradleContent)
                }
            } else {
                build_gradle {
                    writeText(buildGradleContent)
                }
            }

            dir("src/main") {
                dir("kotlin") {
                    kotlinClass("SomeClass", packageName)
                }
            }
            this.mutator()
        }
    }

    private fun plugins(): PluginsSpec =
        plugins {
            id("kotlin")
        }.plus(plugins)
}
