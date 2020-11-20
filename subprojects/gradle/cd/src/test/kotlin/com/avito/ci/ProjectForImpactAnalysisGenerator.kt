package com.avito.ci

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import java.io.File

internal fun generateProjectWithImpactAnalysis(rootDir: File) {
    TestProjectGenerator(
        plugins = listOf("com.avito.android.impact"),
        modules = listOf(
            appModule(
                "appA",
                dependencies = "implementation project(':shared')"
            ),
            appModule(
                "appB",
                dependencies = "implementation project(':shared')"
            ),
            AndroidLibModule(
                name = "shared",
                dependencies = "implementation project(':transitive')"
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

private fun appModule(name: String, dependencies: String) = AndroidAppModule(
    name = name,
    dependencies = dependencies,
    plugins = listOf(
        "com.avito.android.signer",
        "com.avito.android.cd"
    ),
    customScript = """
        import com.avito.cd.BuildVariant
        android {
            buildTypes {
                release {
                    minifyEnabled true
                    proguardFile("proguard.pro")
                }
            }
        }
        signService {
            host("https://signer/")
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
