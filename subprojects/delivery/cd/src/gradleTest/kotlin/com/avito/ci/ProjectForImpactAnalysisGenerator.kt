package com.avito.ci

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal fun generateProjectWithImpactAnalysis(rootDir: File) {
    TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.impact")
        },
        modules = listOf(
            appModule(
                "appA",
                dependencies = setOf(project(":shared"))
            ),
            appModule(
                "appB",
                dependencies = setOf(project(":shared"))
            ),
            AndroidLibModule(
                name = "shared",
                dependencies = setOf(project(":transitive"))
            ),
            AndroidLibModule(
                name = "transitive"
            ),
            AndroidLibModule(
                name = "independent"
            )
        )
    ).generateIn(rootDir)
}

@Suppress("MaxLineLength")
private fun appModule(name: String, dependencies: Set<GradleDependency>) = AndroidAppModule(
    name = name,
    dependencies = dependencies,
    plugins = plugins {
        id("com.avito.android.signer")
        id("com.avito.android.cd")
    },
    imports = listOf(
        "import com.avito.cd.BuildVariant"
    ),
    buildGradleExtra = """
        android {
            buildTypes {
                release {
                    minifyEnabled true
                    proguardFile("proguard.pro")
                }
            }
        }
        signService {
            url.set("https://signer/")
        }                            
        builds {
            fastCheck {
                useImpactAnalysis = true
                unitTests {}
                artifacts {
                    apk("debugApk", BuildVariant.DEBUG, "com.appA", "${'$'}{project.buildDir}/outputs/apk/debug/appA-debug.apk") {}
                }
            }
        }
    """.trimIndent()
)
