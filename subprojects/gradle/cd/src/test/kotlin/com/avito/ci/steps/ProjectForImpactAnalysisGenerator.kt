package com.avito.ci.steps

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import java.io.File


internal fun generateProjectWithImpactAnalysis(rootDir: File) {
    TestProjectGenerator(
        plugins = listOf("com.avito.android.impact"),
        modules = listOf(
            AndroidAppModule(
                name = "appA",
                dependencies = "implementation project(':shared')",
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
            ),
            AndroidAppModule(
                name = "appB",
                dependencies = "implementation project(':shared')",
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
